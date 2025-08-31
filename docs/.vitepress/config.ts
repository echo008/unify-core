import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Unify KMP',
  description: 'Kotlin Multiplatform 跨平台开发框架 - 一套代码，多端复用',
  lang: 'zh-CN',
  base: '/unify-core/',
  
  head: [
    ['link', { rel: 'icon', href: '/logo.svg' }],
    ['meta', { name: 'theme-color', content: '#3b82f6' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:locale', content: 'zh-CN' }],
    ['meta', { property: 'og:title', content: 'Unify KMP | Kotlin Multiplatform 跨平台开发框架' }],
    ['meta', { property: 'og:description', content: '一套代码，多端复用 - 支持8大平台的现代化跨平台开发框架' }],
    ['meta', { property: 'og:site_name', content: 'Unify KMP' }],
    ['meta', { property: 'og:url', content: 'https://echo008.github.io/unify-core/' }],
    ['meta', { name: 'keywords', content: 'Kotlin Multiplatform, 跨平台开发, Android, iOS, Web, Desktop, HarmonyOS' }]
  ],
  
  themeConfig: {
    logo: '/logo.svg',
    siteTitle: 'Unify KMP',
    
    nav: [
      { text: '首页', link: '/' },
      { 
        text: '开始使用',
        items: [
          { text: '快速开始', link: '/guide/start' },
          { text: '项目介绍', link: '/guide/introduction' },
          { text: '教程中心', link: '/tutorials/' }
        ]
      },
      { 
        text: '开发文档',
        items: [
          { text: 'API 参考', link: '/api/' },
          { text: '核心架构', link: '/core/' },
          { text: '开发指南', link: '/guide/' }
        ]
      },
      { text: '示例项目', link: '/examples/' },
      { 
        text: '平台支持',
        items: [
          { text: 'Android', link: '/platforms/android' },
          { text: 'iOS', link: '/platforms/ios' },
          { text: 'Web', link: '/platforms/web' },
          { text: 'Desktop', link: '/platforms/desktop' },
          { text: 'HarmonyOS', link: '/platforms/harmonyos' },
          { text: '小程序', link: '/platforms/miniprogram' },
          { text: 'Watch', link: '/platforms/watch' },
          { text: 'TV', link: '/platforms/tv' }
        ]
      },
      { 
        text: '社区',
        items: [
          { text: '贡献指南', link: '/contributing/' },
          { text: '项目报告', link: '/reports/' },
          { text: 'GitHub', link: 'https://github.com/unify-kmp/unify-core' }
        ]
      }
    ],

    sidebar: {
      '/core/': [
        {
          text: '核心文档',
          items: [
            { text: '文档概览', link: '/core/' },
            { text: '架构设计', link: '/core/architecture' },
            { text: '开发者指南', link: '/core/developer' }
          ]
        }
      ],
      
      '/guide/': [
        {
          text: '快速入门',
          items: [
            { text: '项目介绍', link: '/guide/introduction' },
            { text: '快速开始', link: '/guide/start' },
            { text: '核心概念', link: '/guide/core_concepts' },
            { text: '项目结构', link: '/guide/project_structure' }
          ]
        },
        {
          text: '核心功能',
          items: [
            { text: '状态管理', link: '/guide/state_management' },
            { text: '高级教程', link: '/guide/advanced' }
          ]
        },
        {
          text: '开发指南',
          items: [
            { text: '集成指南', link: '/guide/integration' },
            { text: '最佳实践', link: '/guide/practices' },
            { text: '工具链', link: '/guide/toolchain' },
            { text: '开发工具', link: '/guide/tools' }
          ]
        },
        {
          text: '生产部署',
          items: [
            { text: '生产环境', link: '/guide/production' },
            { text: '部署指南', link: '/guide/deployment' },
            { text: '发布指南', link: '/guide/publishing' },
            { text: '热更新', link: '/guide/hot_update' }
          ]
        },
        {
          text: '运维指南',
          items: [
            { text: '故障排除', link: '/guide/troubleshooting' }
          ]
        }
      ],
      
      '/api/': [
        {
          text: 'API 文档',
          items: [
            { text: 'API 概览', link: '/api/' },
            { text: 'API 参考', link: '/api/api_reference' },
            { text: '核心 API', link: '/api/core' },
            { text: '核心API参考', link: '/api/core_api' },
            { text: 'UI 组件', link: '/api/ui' },
            { text: '平台接口', link: '/api/platforms' },
            { text: '工具类', link: '/api/utilities' },
            { text: '完整文档', link: '/api/api_docs' }
          ]
        }
      ],
      
      '/platforms/': [
        {
          text: '移动平台',
          items: [
            { text: 'Android', link: '/platforms/android' },
            { text: 'iOS', link: '/platforms/ios' },
            { text: 'HarmonyOS', link: '/platforms/harmonyos' }
          ]
        },
        {
          text: '桌面平台',
          items: [
            { text: 'Web', link: '/platforms/web' },
            { text: 'Desktop', link: '/platforms/desktop' }
          ]
        },
        {
          text: '其他平台',
          items: [
            { text: '小程序', link: '/platforms/miniprogram' },
            { text: 'Watch', link: '/platforms/watch' },
            { text: 'TV', link: '/platforms/tv' }
          ]
        }
      ],
      
      '/examples/': [
        {
          text: '基础示例',
          items: [
            { text: '示例概览', link: '/examples/' },
            { text: 'Hello World', link: '/examples/hello_world' },
            { text: '计数器应用', link: '/examples/counter_app' }
          ]
        },
        {
          text: '进阶示例',
          items: [
            { text: 'Todo 应用', link: '/examples/todo_app' },
            { text: '天气应用', link: '/examples/weather_app' },
            { text: 'AI 应用', link: '/examples/ai_app' }
          ]
        }
      ],
      
      '/tutorials/': [
        {
          text: '教程中心',
          items: [
            { text: '学习路径', link: '/tutorials/' }
          ]
        }
      ],
      
      '/contributing/': [
        {
          text: '社区贡献',
          items: [
            { text: '贡献指南', link: '/contributing/contributing' },
            { text: '社区建设', link: '/contributing/community' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/echo008/unify-core' }
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright 2024 Unify KMP Team'
    },

    editLink: {
      pattern: 'https://github.com/echo008/unify-core/edit/main/docs/:path',
      text: '在 GitHub 上编辑此页'
    },

    search: {
      provider: 'local',
      options: {
        locales: {
          'zh-CN': {
            translations: {
              button: {
                buttonText: '搜索文档',
                buttonAriaLabel: '搜索文档'
              },
              modal: {
                noResultsText: '无法找到相关结果',
                resetButtonTitle: '清除查询条件',
                footer: {
                  selectText: '选择',
                  navigateText: '切换',
                  closeText: '关闭'
                }
              }
            }
          }
        }
      }
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'medium'
      }
    },

    docFooter: {
      prev: '上一页',
      next: '下一页'
    },

    outline: {
      label: '页面导航',
      level: [2, 3]
    },

    returnToTopLabel: '回到顶部',
    sidebarMenuLabel: '菜单',
    darkModeSwitchLabel: '主题',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式'
  },

  markdown: {
    lineNumbers: true,
    config: (md) => {
      // 自定义 markdown 配置
    }
  },

  vite: {
    build: {
      chunkSizeWarningLimit: 1600
    }
  },

  sitemap: {
    hostname: 'https://echo008.github.io/unify-core'
  }
})
