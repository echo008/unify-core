import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Unify KMP',
  description: 'Kotlin Multiplatform 跨平台开发框架',
  lang: 'zh-CN',
  
  themeConfig: {
    logo: '/logo.svg',
    
    nav: [
      { text: '首页', link: '/' },
      { text: '快速开始', link: '/guide/getting_started' },
      { text: 'API 参考', link: '/api/' },
      { text: '示例', link: '/examples/' },
      { 
        text: '平台指南',
        items: [
          { text: 'Android', link: '/platforms/android' },
          { text: 'iOS', link: '/platforms/ios' },
          { text: 'Web', link: '/platforms/web' },
          { text: '桌面端', link: '/platforms/desktop' },
          { text: 'HarmonyOS', link: '/platforms/harmonyos' },
          { text: '小程序', link: '/platforms/miniprogram' }
        ]
      },
      { text: 'GitHub', link: 'https://github.com/unify-kmp/unify-core' }
    ],

    sidebar: {
      '/guide/': [
        {
          text: '开始使用',
          items: [
            { text: '项目介绍', link: '/guide/introduction' },
            { text: '快速开始', link: '/guide/getting_started' }
          ]
        },
        {
          text: '进阶指南',
          items: [
            { text: '高级教程和最佳实践', link: '/guide/advanced' },
            { text: '故障排除指南', link: '/guide/troubleshooting' },
            { text: '部署指南', link: '/guide/deployment' }
          ]
        }
      ],
      
      '/platforms/': [
        {
          text: '平台开发指南',
          items: [
            { text: 'Android', link: '/platforms/android' },
            { text: 'iOS', link: '/platforms/ios' },
            { text: 'Web', link: '/platforms/web' },
            { text: '桌面端', link: '/platforms/desktop' },
            { text: 'HarmonyOS', link: '/platforms/harmonyos' },
            { text: '小程序', link: '/platforms/miniprogram' }
          ]
        }
      ],
      
      '/api/': [
        {
          text: 'API 参考',
          items: [
            { text: '核心 API', link: '/api/core' },
            { text: 'UI 组件', link: '/api/ui_components' },
            { text: '平台接口', link: '/api/platform_interfaces' },
            { text: '工具类', link: '/api/utilities' }
          ]
        }
      ],
      
      '/examples/': [
        {
          text: '示例项目',
          items: [
            { text: 'Hello World', link: '/examples/hello_world' },
            { text: '计数器应用', link: '/examples/counter_app' },
            { text: 'Todo 应用', link: '/examples/todo_app' },
            { text: '天气应用', link: '/examples/weather_app' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/unify-kmp/unify-core' }
    ],

    footer: {
      message: '基于 MIT 许可证发布',
      copyright: 'Copyright © 2024 Unify KMP Team'
    },

    search: {
      provider: 'local'
    },

    editLink: {
      pattern: 'https://github.com/unify-kmp/unify-core/edit/main/docs/:path',
      text: '在 GitHub 上编辑此页'
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'medium'
      }
    }
  },

  markdown: {
    lineNumbers: true,
    theme: {
      light: 'github-light',
      dark: 'github-dark'
    }
  },

  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['meta', { name: 'theme-color', content: '#3c82f6' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:locale', content: 'zh-CN' }],
    ['meta', { property: 'og:title', content: 'Unify KMP | Kotlin Multiplatform 跨平台开发框架' }],
    ['meta', { property: 'og:site_name', content: 'Unify KMP' }],
    ['meta', { property: 'og:url', content: 'https://unify-kmp.github.io/' }]
  ]
})
