<p align="center">
  <a href="https://liuruis.cn/" target="_blank">
    <img src="./pic/NBlog.png" alt="NBlog logo" style="width: 200px; height: 200px">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-1.8+-orange" alt="JDK">
  <img src="https://img.shields.io/badge/SpringBoot-2.2.7.RELEASE-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MyBatis-3.5.5-red" alt="MyBatis">
  <img src="https://img.shields.io/badge/Vue-2.6.11-brightgreen" alt="Vue">
  <img src="https://img.shields.io/badge/license-MIT-blue" alt="License">
</p>

# NBlog

> Spring Boot + Vue「前后端分离，人不分离」博客系统。

自用博客，长期维护，欢迎勘误。

## 预览地址

- 前台：[https://liuruis.cn](https://liuruis.cn)
- 后台：[https://admin.liuruis.cn](https://admin.liuruis.cn)

## 目录

- [项目简介](#项目简介)
- [项目结构](#项目结构)
- [技术栈](#技术栈)
  - [后端](#后端)
  - [前端](#前端)
  - [后台 UI](#后台-ui)
  - [前台 UI](#前台-ui)
- [快速开始](#快速开始)
- [开发环境](#开发环境)
- [注意事项](#注意事项)
- [隐藏功能](#隐藏功能)
- [License](#license)
- [Thanks](#thanks)

## 项目简介

NBlog 是一个前后端分离的博客系统，包含博客前台展示与后台管理两部分，适合作为个人博客、内容管理系统或相关项目的参考实现。

## 项目结构

```text
blog
├─ blog-api    # Spring Boot 后端服务
├─ blog-cms    # Vue 2 后台管理端
├─ blog-view   # Vue 2 前台展示端
└─ pic         # README 相关图片资源
```

## 技术栈

### 后端

1. 核心框架：[Spring Boot](https://github.com/spring-projects/spring-boot)
2. 安全框架：[Spring Security](https://github.com/spring-projects/spring-security)
3. Token：[jjwt](https://github.com/jwtk/jjwt)
4. ORM 框架：[MyBatis](https://github.com/mybatis/spring-boot-starter)
5. 分页插件：[PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)
6. NoSQL 缓存：[Redis](https://github.com/redis/redis)
7. Markdown 转 HTML：[commonmark-java](https://github.com/commonmark/commonmark-java)
8. 离线 IP 地址库：[ip2region](https://github.com/lionsoul2014/ip2region)
9. 定时任务：[quartz](https://github.com/quartz-scheduler/quartz)
10. UserAgent 解析：[yauaa](https://github.com/nielsbasjes/yauaa)

邮件模板参考自 [Typecho-CommentToMail-Template](https://github.com/MisakaTAT/Typecho-CommentToMail-Template)。

### 前端

- 核心框架：Vue 2.x、Vue Router、Vuex
- Vue 项目基于 `@vue/cli` 4.x 构建
- JS 依赖及参考的 CSS：[axios](https://github.com/axios/axios)、[moment](https://github.com/moment/moment)、[nprogress](https://github.com/rstacruz/nprogress)、[v-viewer](https://github.com/fengyuanchen/viewerjs)、[prismjs](https://github.com/PrismJS/prism)、[APlayer](https://github.com/DIYgod/APlayer)、[MetingJS](https://github.com/metowolf/MetingJS)、[lodash](https://github.com/lodash/lodash)、[mavonEditor](https://github.com/hinesboy/mavonEditor)、[echarts](https://github.com/apache/echarts)、[tocbot](https://github.com/tscanlin/tocbot)、[iCSS](https://github.com/chokcoco/iCSS)

> 由 [@willWang8023](https://github.com/willWang8023) 维护的 Vue3 版本请查看 [blog-view-vue3](https://github.com/willWang8023/blog-view-vue3)。

### 后台 UI

- 后台基于 [vue-admin-template](https://github.com/PanJiaChen/vue-admin-template) 二次修改后的 [my-vue-admin-template](https://github.com/Naccl/my-vue-admin-template) 模板进行开发（[于 2021 年 11 月 1 日重构](https://github.com/Naccl/NBlog/commit/b33641fe34b2bed34e8237bacf67146cd64be4cf)）
- UI 框架为 [Element UI](https://github.com/ElemeFE/element)

### 前台 UI

- [Semantic UI](https://semantic-ui.com/)：主要使用，页面布局样式，个人感觉挺好看的 UI 框架，比较适合前台界面的开发，语义化的 CSS。前一版博客系统使用过，可惜该框架 Vue 版的开发完成度不高，见 [Semantic UI Vue](https://semantic-ui-vue.github.io/#/)
- [Element UI](https://github.com/ElemeFE/element)：部分使用，一些小组件，弥补了 Semantic UI 的不足，便于快速实现效果
- 文章排版：基于 [typo.css](https://github.com/sofish/typo.css) 修改

## 快速开始

### 1. 准备环境

- JDK 1.8+
- MySQL
- Redis
- Node.js / npm

### 2. 初始化数据库

- 创建 MySQL 数据库 `nblog`
- 执行 `/blog-api/nblog.sql` 初始化表结构与基础数据

### 3. 配置后端

修改后端配置文件：

```text
/blog-api/src/main/resources/application-dev.properties
```

### 4. 启动服务

先启动后端：

```bash
cd blog-api
mvn spring-boot:run
```

再分别启动后台和前台项目：

```bash
cd blog-cms
npm install
npm run serve
```

```bash
cd blog-view
npm install
npm run serve
```

## 开发环境

1. 创建 MySQL 数据库 `nblog`，并执行 `/blog-api/nblog.sql` 初始化表数据
2. 修改配置信息 `/blog-api/src/main/resources/application-dev.properties`
3. 安装 Redis 并启动
4. 启动后端服务
5. 分别在 `blog-cms` 和 `blog-view` 目录下执行 `npm install` 安装依赖
6. 分别在 `blog-cms` 和 `blog-view` 目录下执行 `npm run serve` 启动前后台页面

## 注意事项

一些常见问题：

- MySQL 确保数据库字符集为 `utf8mb4`（“站点设置”及“文章详情”等许多表字段需要 `utf8mb4` 格式字符集来支持 emoji 表情，否则在导入 SQL 文件时，即使成功导入，也会有部分字段内容不完整，导致前端页面渲染数据时报错）
- 确保 Maven 和 NPM 能够成功导入现版本依赖，请勿升级或降低依赖版本
- 数据库中默认用户名密码为 `Admin`、`123456`。因为是个人博客，没打算做修改密码的页面，可在 `top.naccl.util.HashUtils` 下的 `main` 方法手动生成密码存入数据库
- 注意修改 `application-dev.properties` 的配置信息
  - 注意修改 `token.secretKey`，否则无法保证 token 安全性
  - `spring.mail.host` 及 `spring.mail.port` 的默认配置为阿里云邮箱，其它邮箱服务商可参考关键字 `spring mail 服务器`（邮箱配置用于接收/发送评论提醒）
- 如需部署，注意将 `/blog-view/src/plugins/axios.js` 和 `/blog-cms/src/util/request.js` 中的 `baseUrl` 修改为你的后端 API 地址
- 大部分个性化配置可以在后台“站点设置”中修改，小部分由于考虑到首屏加载速度（如首页大图）需要修改前端源码

## 隐藏功能

- 在前台访问 `/login` 路径登录后，可以以博主身份（带有博主标识）回复评论，且不需要填写昵称和邮箱即可提交
- 在 Markdown 中加入 `<meting-js server="netease" type="song" id="歌曲id" theme="#25CCF7"></meting-js>`（注意以正文形式添加，而不是代码片段）可以在文章中添加 [APlayer](https://github.com/DIYgod/APlayer) 音乐播放器，`netease` 为网易云音乐，其它配置及具体用法参考 [MetingJS](https://github.com/metowolf/MetingJS)
- 提供了两种隐藏文字效果：在 Markdown 中使用 `@@` 包住文字，文字会被渲染成“黑幕”效果，鼠标悬浮在上面时才会显示；使用 `%%` 包住文字，文字会被“蓝色覆盖层”遮盖，只有鼠标选中状态才会反色显示。例如：`@@隐藏文字@@`、`%%隐藏文字%%`

## License

[MIT](https://github.com/SingleLRS/blog/blob/master/LICENSE)

## Thanks

- 感谢 [JetBrains](https://www.jetbrains.com/?from=NBlog) 提供的 Open Source License
- 感谢上面提到的每个开源项目
