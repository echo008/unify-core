#!/bin/bash
set -e

# Unify-Core 生产环境部署脚本
# 使用方法: ./scripts/deploy-production.sh [version]

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查部署依赖..."
    
    local deps=("docker" "kubectl" "helm" "aws" "jq")
    for dep in "${deps[@]}"; do
        if ! command -v $dep &> /dev/null; then
            log_error "缺少依赖: $dep"
            exit 1
        fi
    done
    
    log_success "所有依赖检查通过"
}

# 验证环境变量
validate_environment() {
    log_info "验证环境变量..."
    
    local required_vars=(
        "AWS_REGION"
        "KUBE_CONTEXT"
        "DOCKER_REGISTRY"
        "DB_HOST"
        "REDIS_HOST"
        "KAFKA_BOOTSTRAP_SERVERS"
    )
    
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            log_error "环境变量 $var 未设置"
            exit 1
        fi
    done
    
    log_success "环境变量验证通过"
}

# 构建Docker镜像
build_docker_images() {
    local version=$1
    log_info "构建Docker镜像 (版本: $version)..."
    
    # 构建主应用镜像
    docker build -t ${DOCKER_REGISTRY}/unify-core:${version} \
        --build-arg VERSION=${version} \
        --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
        --build-arg VCS_REF=$(git rev-parse --short HEAD) \
        -f deployment/Dockerfile .
    
    # 构建各平台特定镜像
    docker build -t ${DOCKER_REGISTRY}/unify-core-android:${version} \
        -f deployment/Dockerfile.android .
    
    docker build -t ${DOCKER_REGISTRY}/unify-core-ios:${version} \
        -f deployment/Dockerfile.ios .
    
    docker build -t ${DOCKER_REGISTRY}/unify-core-web:${version} \
        -f deployment/Dockerfile.web .
    
    log_success "Docker镜像构建完成"
}

# 推送Docker镜像
push_docker_images() {
    local version=$1
    log_info "推送Docker镜像到仓库..."
    
    # 登录Docker仓库
    aws ecr get-login-password --region ${AWS_REGION} | \
        docker login --username AWS --password-stdin ${DOCKER_REGISTRY}
    
    # 推送镜像
    docker push ${DOCKER_REGISTRY}/unify-core:${version}
    docker push ${DOCKER_REGISTRY}/unify-core-android:${version}
    docker push ${DOCKER_REGISTRY}/unify-core-ios:${version}
    docker push ${DOCKER_REGISTRY}/unify-core-web:${version}
    
    log_success "Docker镜像推送完成"
}

# 运行预部署测试
run_pre_deployment_tests() {
    log_info "运行预部署测试..."
    
    # 运行单元测试
    ./gradlew test
    
    # 运行集成测试
    ./gradlew integrationTest
    
    # 运行性能基准测试
    ./gradlew performanceBenchmark
    
    # 运行安全审计
    ./gradlew securityAudit
    
    # 验证Docker镜像
    docker run --rm ${DOCKER_REGISTRY}/unify-core:${version} /app/healthcheck.sh
    
    log_success "预部署测试通过"
}

# 备份当前生产环境
backup_production() {
    log_info "备份当前生产环境..."
    
    # 备份数据库
    kubectl exec -n production deployment/postgres -- \
        pg_dump -U ${DB_USERNAME} ${DB_NAME} > backup/db-$(date +%Y%m%d-%H%M%S).sql
    
    # 备份配置
    kubectl get configmap -n production -o yaml > backup/configmaps-$(date +%Y%m%d-%H%M%S).yaml
    kubectl get secret -n production -o yaml > backup/secrets-$(date +%Y%m%d-%H%M%S).yaml
    
    # 备份当前部署状态
    helm get values unify-core -n production > backup/helm-values-$(date +%Y%m%d-%H%M%S).yaml
    
    log_success "生产环境备份完成"
}

# 部署到Kubernetes
deploy_to_kubernetes() {
    local version=$1
    log_info "部署到Kubernetes集群..."
    
    # 切换到生产环境上下文
    kubectl config use-context ${KUBE_CONTEXT}
    
    # 创建命名空间（如果不存在）
    kubectl create namespace production --dry-run=client -o yaml | kubectl apply -f -
    
    # 更新配置
    kubectl apply -f deployment/k8s/configmap.yaml -n production
    kubectl apply -f deployment/k8s/secrets.yaml -n production
    
    # 使用Helm部署应用
    helm upgrade --install unify-core deployment/helm/unify-core \
        --namespace production \
        --set image.tag=${version} \
        --set image.repository=${DOCKER_REGISTRY}/unify-core \
        --values deployment/helm/values-production.yaml \
        --wait --timeout=600s
    
    log_success "Kubernetes部署完成"
}

# 验证部署
verify_deployment() {
    local version=$1
    log_info "验证部署状态..."
    
    # 等待Pod就绪
    kubectl wait --for=condition=ready pod -l app=unify-core -n production --timeout=300s
    
    # 检查服务状态
    kubectl get pods -n production -l app=unify-core
    kubectl get services -n production -l app=unify-core
    
    # 健康检查
    local health_endpoint=$(kubectl get service unify-core -n production -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
    local health_check_url="http://${health_endpoint}/actuator/health"
    
    log_info "等待服务启动..."
    sleep 30
    
    local retry_count=0
    local max_retries=10
    
    while [ $retry_count -lt $max_retries ]; do
        if curl -f -s ${health_check_url} > /dev/null; then
            log_success "健康检查通过"
            break
        else
            log_warning "健康检查失败，重试中... ($((retry_count + 1))/$max_retries)"
            sleep 10
            ((retry_count++))
        fi
    done
    
    if [ $retry_count -eq $max_retries ]; then
        log_error "健康检查失败，部署可能有问题"
        return 1
    fi
    
    # 验证版本
    local deployed_version=$(curl -s ${health_endpoint}/actuator/info | jq -r '.build.version')
    if [ "$deployed_version" != "$version" ]; then
        log_error "部署版本不匹配: 期望 $version, 实际 $deployed_version"
        return 1
    fi
    
    log_success "部署验证通过"
}

# 运行烟雾测试
run_smoke_tests() {
    log_info "运行烟雾测试..."
    
    local base_url=$(kubectl get service unify-core -n production -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
    
    # 基本API测试
    curl -f -s "http://${base_url}/api/v1/health" || {
        log_error "API健康检查失败"
        return 1
    }
    
    # 认证测试
    curl -f -s -X POST "http://${base_url}/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"test","password":"test"}' || {
        log_error "认证API测试失败"
        return 1
    }
    
    # AI服务测试
    curl -f -s -X POST "http://${base_url}/api/v1/ai/chat" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer test-token" \
        -d '{"message":"Hello"}' || {
        log_error "AI服务测试失败"
        return 1
    }
    
    log_success "烟雾测试通过"
}

# 配置监控和告警
setup_monitoring() {
    log_info "配置监控和告警..."
    
    # 部署Prometheus监控
    helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
        --namespace monitoring \
        --create-namespace \
        --values deployment/monitoring/prometheus-values.yaml
    
    # 部署Grafana仪表板
    kubectl apply -f deployment/monitoring/grafana-dashboards.yaml -n monitoring
    
    # 配置告警规则
    kubectl apply -f deployment/monitoring/alert-rules.yaml -n monitoring
    
    # 部署日志收集
    helm upgrade --install fluentd fluent/fluentd \
        --namespace logging \
        --create-namespace \
        --values deployment/logging/fluentd-values.yaml
    
    log_success "监控和告警配置完成"
}

# 更新负载均衡器
update_load_balancer() {
    log_info "更新负载均衡器配置..."
    
    # 更新ALB配置
    kubectl apply -f deployment/k8s/ingress.yaml -n production
    
    # 等待负载均衡器更新
    sleep 60
    
    # 验证负载均衡器状态
    kubectl get ingress -n production
    
    log_success "负载均衡器更新完成"
}

# 清理旧版本
cleanup_old_versions() {
    log_info "清理旧版本资源..."
    
    # 保留最近3个版本的镜像
    local images=$(aws ecr describe-images --repository-name unify-core --region ${AWS_REGION} \
        --query 'sort_by(imageDetails,& imagePushedAt)[:-3].[imageDigest]' --output text)
    
    for digest in $images; do
        aws ecr batch-delete-image --repository-name unify-core --region ${AWS_REGION} \
            --image-ids imageDigest=$digest
    done
    
    # 清理旧的Helm发布
    helm history unify-core -n production --max 5 | tail -n +6 | awk '{print $1}' | \
        xargs -I {} helm delete unify-core --revision {} -n production
    
    log_success "旧版本清理完成"
}

# 发送部署通知
send_deployment_notification() {
    local version=$1
    local status=$2
    
    log_info "发送部署通知..."
    
    local message
    if [ "$status" = "success" ]; then
        message="✅ Unify-Core v${version} 部署成功到生产环境"
    else
        message="❌ Unify-Core v${version} 部署失败"
    fi
    
    # 发送Slack通知
    if [ -n "$SLACK_WEBHOOK_URL" ]; then
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"text\":\"$message\"}" \
            $SLACK_WEBHOOK_URL
    fi
    
    # 发送邮件通知
    if [ -n "$NOTIFICATION_EMAIL" ]; then
        echo "$message" | mail -s "Unify-Core 部署通知" $NOTIFICATION_EMAIL
    fi
    
    log_success "部署通知发送完成"
}

# 回滚函数
rollback_deployment() {
    local previous_version=$1
    log_warning "开始回滚到版本: $previous_version"
    
    # Helm回滚
    helm rollback unify-core -n production
    
    # 等待回滚完成
    kubectl wait --for=condition=ready pod -l app=unify-core -n production --timeout=300s
    
    # 验证回滚
    verify_deployment $previous_version
    
    log_success "回滚完成"
}

# 主函数
main() {
    local version=${1:-$(git describe --tags --abbrev=0)}
    
    log_info "开始部署 Unify-Core v${version} 到生产环境"
    
    # 检查参数
    if [ -z "$version" ]; then
        log_error "请提供版本号"
        echo "使用方法: $0 <version>"
        exit 1
    fi
    
    # 确认部署
    echo -n "确认部署版本 $version 到生产环境? (y/N): "
    read -r confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        log_info "部署已取消"
        exit 0
    fi
    
    # 记录开始时间
    local start_time=$(date +%s)
    
    # 执行部署步骤
    trap 'log_error "部署过程中发生错误，正在回滚..."; rollback_deployment; exit 1' ERR
    
    check_dependencies
    validate_environment
    backup_production
    build_docker_images $version
    push_docker_images $version
    run_pre_deployment_tests
    deploy_to_kubernetes $version
    verify_deployment $version
    run_smoke_tests
    setup_monitoring
    update_load_balancer
    cleanup_old_versions
    
    # 计算部署时间
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    log_success "部署完成! 耗时: ${duration}秒"
    send_deployment_notification $version "success"
    
    # 显示部署信息
    echo ""
    echo "=========================================="
    echo "部署信息:"
    echo "版本: $version"
    echo "环境: 生产环境"
    echo "集群: $KUBE_CONTEXT"
    echo "镜像: ${DOCKER_REGISTRY}/unify-core:${version}"
    echo "服务地址: $(kubectl get service unify-core -n production -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')"
    echo "监控地址: $(kubectl get service prometheus-grafana -n monitoring -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')"
    echo "=========================================="
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
