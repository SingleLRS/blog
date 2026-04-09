# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库概览

这是一个前后端分离的博客系统仓库，包含 3 个主要子项目：

- `blog-api`：Spring Boot 2.2 后端接口，负责认证、内容管理、评论、统计、日志、缓存、定时任务等
- `blog-cms`：Vue 2 后台管理端，负责文章/动态/分类/标签/评论/站点设置/日志/统计/图床管理
- `blog-view`：Vue 2 博客前台，负责首页、文章详情、归档、分类、标签、动态、友链、关于页等

仓库根目录没有统一的构建入口，开发时通常进入对应子项目目录执行命令。

## 常用命令

### 后端：`blog-api`

开发前提：
- JDK 8+
- MySQL
- Redis
- 先导入数据库脚本 `blog-api/nblog.sql`
- 修改配置文件 `blog-api/src/main/resources/application-dev.properties`

```bash
cd blog-api
mvn spring-boot:run
mvn test
mvn test -Dtest=BlogApiApplicationTests
mvn test -Dtest=BlogApiApplicationTests#contextLoads
mvn clean package
```

### 后台前端：`blog-cms`

```bash
cd blog-cms
npm install
npm run serve
npm run build
```

### 前台前端：`blog-view`

```bash
cd blog-view
npm install
npm run serve
npm run build
npm run patch:semantic
```

说明：
- `blog-view` 的 `postinstall` 会自动执行 `scripts/patch-semantic-css.js`
- 如果重装依赖后前台样式异常，可手动执行 `npm run patch:semantic`
- 两个 Vue 项目的 `package.json` 都没有定义 lint 和 test 脚本

## 运行配置

- 后端默认启用 `dev` profile：`blog-api/src/main/resources/application.properties`
- 前端接口地址是直接写在源码中的，不是通过环境变量注入：
  - `blog-cms/src/util/request.js`：后台接口 base URL
  - `blog-view/src/plugins/axios.js`：前台接口 base URL
- 本地文件上传访问映射由后端 `WebConfig` 和 `upload.*` 配置共同控制

## 高层架构

### 后端分层结构

后端整体是典型的分层结构：

- `controller`、`controller/admin`：接口入口，分别对应前台接口和后台接口
- `service`、`service/impl`：业务逻辑层
- `mapper` + `src/main/resources/mapper/*.xml`：MyBatis 持久层
- `entity`：数据库实体
- `model/dto`、`model/vo`：请求/响应模型，与实体分离
- `config`、`interceptor`、`aspect`、`handler`：安全、跨域、拦截、日志、异常处理等横切逻辑

典型链路：

`controller -> service -> mapper/xml -> MySQL`

其中 Redis 不是可有可无的附加组件，而是核心运行链路的一部分。

### 前后台接口分离方式

前后台并不是两个后端服务，而是在同一个 Spring Boot 应用里按 URL 前缀分开：

- 前台公开接口大多是普通路径，如 `/blogs`、`/blog`、`/site`
- 后台管理接口统一放在 `/admin/**`

`SecurityConfig` 的关键规则：

- `/admin/**` 都要经过 JWT 校验
- 后台 `GET` 请求允许 `admin` 和 `visitor` 角色访问
- 后台非 `GET` 请求只允许 `admin` 角色访问
- 登录通过 JWT 过滤器处理，不依赖服务端 session

这套划分会直接映射到两个前端：

- `blog-view` 只消费公开接口
- `blog-cms` 只消费 `/admin/` 接口，并把 token 保存在 `localStorage`

### Redis 在业务中的作用

Redis 被深度用于缓存和统计，改代码时不能把它当成单纯的性能优化层。

重点场景：
- 博客浏览量在应用启动时加载到 Redis，并优先在 Redis 中递增
- 首页博客列表、最新推荐、归档等数据会在 `BlogServiceImpl` 中缓存
- 访客身份标识和 UV 统计依赖 Redis Set
- 后台仪表盘部分统计数据依赖 Redis + MySQL 组合计算

如果直接改 MySQL 数据而没有走应用逻辑，Redis 缓存和数据库之间可能会短暂或持续不一致。

### AOP 日志与访客追踪

后端有不少重要逻辑不在 controller 里，而是在切面里：

- `@VisitLogger` + `VisitLogAspect`：记录前台访问行为，同时签发/校验访客 `identification`
- `@OperationLogger`：记录后台操作日志
- 异常日志也通过切面统一记录

因此修改 controller 参数、接口返回值或访问路径时，要一起检查相关 aspect，避免日志、统计、访客标识逻辑失效。

### 内容域模型

核心业务对象包括：

- Blog
- Moment
- Category
- Tag
- Comment
- Friend
- SiteSetting

其中几个要点：
- 博客、分类、标签、评论管理主要集中在后台管理接口和对应 service 中
- 博客与标签关系不是 ORM 自动维护，而是通过业务逻辑显式维护 `blog_tag`
- 站点信息、关于我、首页侧边栏等配置很多来自后端站点设置，不只是前端静态常量

### 后台前端：`blog-cms`

`blog-cms` 基于 Vue 2 + Vue Router + Vuex + Element UI，整体是典型后台管理台结构。

主要模块按路由划分：
- Dashboard
- 博客管理（文章、动态、分类、标签、评论）
- 页面管理（站点设置、友链、关于我）
- 图床管理
- 系统管理
- 日志管理
- 统计管理

关键结构：
- `src/api`：后台接口封装
- `src/router/index.js`：后台路由与菜单结构
- `src/store`：全局状态
- `src/layout`：后台整体框架布局
- `src/util/request.js`：Axios 实例、token 注入、统一错误提示

路由守卫逻辑比较简单：
- 除 `/login` 外，其余页面都要求 `localStorage` 中有 token
- 真正权限控制仍以后端为准，不要只改前端守卫

### 前台前端：`blog-view`

`blog-view` 同样基于 Vue 2 + Vue Router + Vuex，但结构更偏内容展示站点。

主要页面按路由划分：
- 首页
- 归档
- 文章详情
- 标签页
- 分类页
- 动态页
- 友链页
- 关于页
- 前台登录页

关键结构：
- `src/api`：前台接口封装
- `src/router/index.js`：前台页面路由
- `src/store`：站点信息、评论状态、文章密码框、专注模式等共享状态
- `src/plugins/axios.js`：统一请求逻辑与访客标识处理

### 前台访客标识机制

前台不是完全匿名无状态的。

`blog-view` 会在 `localStorage` 中保存 `identification`，流程是：

- 前端请求公开接口时带上 `identification`
- 后端 `VisitLogAspect` 校验或签发该标识
- 后端把 `identification` 通过响应头返回
- 前端 Axios 响应拦截器再写回本地

这个标识和以下能力相关：
- 访客追踪
- UV 统计
- 评论相关行为
- 访问日志记录

如果改动评论、统计、请求头处理或跨域暴露头部时，要一起检查这条链路。

## 启动与联调顺序

根据 README，标准本地联调顺序是：

1. 创建 MySQL 数据库 `nblog`
2. 使用 `blog-api/nblog.sql` 初始化数据
3. 修改 `blog-api/src/main/resources/application-dev.properties`
4. 启动 Redis
5. 启动后端 `blog-api`
6. 分别进入 `blog-cms` 和 `blog-view` 执行 `npm install`
7. 分别启动两个前端 `npm run serve`

## README 中提到的重要注意事项

- MySQL 字符集要使用 `utf8mb4`，否则部分含 emoji 的内容会导致数据或前端渲染异常
- 尽量不要随意升级或降级 Maven/NPM 依赖版本
- 如果部署到自己的环境，需要修改：
  - `blog-view/src/plugins/axios.js`
  - `blog-cms/src/util/request.js`
  里的后端 API 地址
- 很多个性化配置可在后台“站点设置”里修改，不一定都要改前端源码

## 需要优先查看的文件

在做较大改动前，优先阅读这些文件：

- `blog-api/src/main/resources/application-dev.properties`：本地开发核心配置
- `blog-api/src/main/java/top/naccl/config/SecurityConfig.java`：认证与前后台访问规则
- `blog-api/src/main/java/top/naccl/config/WebConfig.java`：跨域、拦截器、静态资源映射
- `blog-api/src/main/java/top/naccl/aspect/VisitLogAspect.java`：访客标识与访问日志
- `blog-api/src/main/java/top/naccl/service/impl/BlogServiceImpl.java`：博客缓存、浏览量、首页数据逻辑
- `blog-cms/src/util/request.js`：后台请求封装与 token 处理
- `blog-view/src/plugins/axios.js`：前台请求封装与 identification 处理
