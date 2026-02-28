# 启航电商ERP系统2.0 - 项目Wiki

> **版本**: v2.0.1  
> **最后更新**: 2026年2月12日  
> **维护者**: 启航科技团队

---

## 📋 目录

- [项目简介](#项目简介)
- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [功能模块](#功能模块)
- [API接口文档](#api接口文档)
- [部署指南](#部署指南)
- [开发规范](#开发规范)
- [常见问题](#常见问题)

---

## 🎯 项目简介

### 概述

启航电商ERP系统2.0是一个完整开箱即用的开源电商ERP系统，专注于为电商企业提供多平台订单管理、商品管理、库存管理、发货管理等全方位的运营支持。

### 核心特点

✅ **多平台支持**: 支持淘宝天猫、京东、拼多多、抖店、微信小店、快手小店等主流电商平台  
✅ **订单处理**: 自动拉取订单、批量审核、智能分配发货、电子面单打印  
✅ **库存管理**: 实时库存监控、自动预警、多仓库管理  
✅ **供应链协同**: 采购管理、供应商管理、出入库管理  
✅ **数据分析**: 销售数据统计、成本核算、利润分析  
✅ **开放API**: 提供标准化接口，支持与外部系统集成  

### 适用场景

- 🏪 **中大型电商企业**: 多平台多店铺运营，日订单量500+
- 🏭 **品牌制造商**: 自有品牌，需要供应链管理
- 🚚 **电商服务商**: 为多个商家提供订单处理服务
- 💼 **MCN机构**: 直播带货、社交电商运营

### 系统演进

- **v1.0**: 基础订单管理功能
- **v2.0**: 完整ERP系统，增加采购、库存、供应链管理
- **v2.5.4**: 优化数据结构，增强店铺商品管理
- **v2.6.1**: 完善发货中心，增加供应商备货管理

---

## 🏗️ 系统架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层 (Vue2 + Element UI)              │
│                         Nginx (静态资源 + 反向代理)              │
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────┐
│                      API网关层 (Spring Boot 3)                 │
│              - 认证授权 (JWT + Spring Security)                │
│              - 请求路由                                         │
│              - 接口文档 (Swagger/SpringDoc)                     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                         业务服务层                              │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐   │
│  │  系统服务 │  商品服务 │  订单服务 │  库存服务 │  店铺服务 │   │
│  │   (sys)  │ (goods)  │ (order)  │ (stock)  │  (shop)  │   │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘   │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐   │
│  │  淘宝服务 │  京东服务 │  拼多多   │  抖店服务 │ 微信服务  │   │
│  │   (tao)  │   (jd)   │  (pdd)   │  (dou)   │  (wei)   │   │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                         数据层                                 │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │   MySQL 8.0     │  │    Redis 7      │                   │
│  │  (主数据存储)    │  │  (缓存/会话)     │                   │
│  └─────────────────┘  └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     第三方平台集成                              │
│  淘宝开放平台 | 京东开放平台 | 拼多多开放平台 | 抖店开放平台      │
│  微信开放平台 | 快手开放平台 | 物流公司API                       │
└─────────────────────────────────────────────────────────────┘
```

### 项目结构

```
qihangerp2-open/
├── api/                          # API网关服务
│   ├── src/main/java/
│   │   └── cn/qihangerp/api/
│   │       ├── ApiApplication.java      # 启动类
│   │       ├── config/                  # 配置类
│   │       ├── controller/              # 控制器
│   │       ├── dou/                     # 抖店相关
│   │       ├── jd/                      # 京东相关
│   │       ├── pdd/                     # 拼多多相关
│   │       ├── tao/                     # 淘宝相关
│   │       └── wei/                     # 微信相关
│   ├── src/main/resources/
│   │   └── application.yaml             # 配置文件
│   └── pom.xml
│
├── core/                         # 核心模块
│   ├── common/                   # 公共工具类
│   ├── interfaces/               # 接口定义
│   ├── mapper/                   # 数据访问层
│   ├── model/                    # 数据模型
│   └── security/                 # 安全认证
│
├── service/                      # 业务服务模块
│   ├── dou/                      # 抖店服务
│   ├── erp/                      # ERP核心服务
│   ├── goods/                    # 商品服务
│   ├── jd/                       # 京东服务
│   ├── order/                    # 订单服务
│   ├── pdd/                      # 拼多多服务
│   ├── shop/                     # 店铺服务
│   ├── stock/                    # 库存服务
│   ├── sys/                      # 系统服务
│   ├── tao/                      # 淘宝服务
│   └── wei/                      # 微信服务
│
├── vue/                          # 前端项目
│   ├── public/                   # 静态资源
│   ├── src/
│   │   ├── api/                  # API接口封装
│   │   ├── assets/               # 资源文件
│   │   ├── components/           # 公共组件
│   │   ├── layout/               # 布局组件
│   │   ├── router/               # 路由配置
│   │   ├── store/                # Vuex状态管理
│   │   ├── utils/                # 工具类
│   │   └── views/                # 页面视图
│   ├── package.json
│   └── vue.config.js
│
├── docs/                         # 文档
│   └── qihang-erp.sql           # 数据库脚本
│
├── pom.xml                       # Maven父项目配置
└── README.md                     # 项目说明
```

### 模块说明

#### API模块
- **功能**: 统一API网关，处理所有HTTP请求
- **端口**: 8087
- **职责**: 请求路由、权限验证、接口文档生成

#### Core模块
- **common**: 公共工具类、异常处理、结果封装
- **interfaces**: 服务接口定义
- **mapper**: MyBatis-Plus数据访问层
- **model**: 实体类、DTO、VO
- **security**: Spring Security配置、JWT认证

#### Service模块
- **erp**: ERP核心业务（采购、出入库、备货单等）
- **goods**: 商品管理（商品库、分类、品牌、供应商）
- **order**: 订单管理（订单库、发货、售后）
- **stock**: 库存管理（库存、入库、出库、仓库）
- **shop**: 店铺管理（店铺信息、平台配置）
- **sys**: 系统管理（用户、角色、菜单、权限）
- **tao/jd/pdd/dou/wei**: 各平台对接服务

---

## 💻 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK版本 |
| Spring Boot | 3.0.2 | 应用框架 |
| Spring Security | 6.0.x | 安全框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 7.0+ | 缓存/会话存储 |
| JWT | 0.11.5 | Token认证 |
| Lombok | 1.18.30 | 简化代码 |
| FastJSON2 | 2.0.43 | JSON处理 |
| SpringDoc | 2.3.0 | API文档 |
| MapStruct | 1.6.3 | 对象映射 |
| Apache POI | 5.2.5 | Excel操作 |
| Undertow | - | Web容器 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 2.6.12 | 前端框架 |
| Vue Router | 3.4.9 | 路由管理 |
| Vuex | 3.6.0 | 状态管理 |
| Element UI | 2.15.13 | UI组件库 |
| Axios | 0.24.0 | HTTP客户端 |
| ECharts | 5.4.0 | 图表库 |
| js-cookie | 3.0.1 | Cookie操作 |
| JSEncrypt | 3.0.0-rc.1 | RSA加密 |

### 开发工具

| 工具 | 版本 | 说明 |
|------|------|------|
| Maven | 3.8+ | 项目构建 |
| Node.js | 16.x | 前端运行环境 |
| npm | 7.x+ | 包管理 |
| Git | 2.x | 版本控制 |
| Docker | 20.x+ | 容器化部署 |
| Nginx | 1.20+ | Web服务器 |

### 第三方SDK

- **淘宝开放平台SDK**: 订单、商品同步
- **京东开放平台SDK**: open-sdk-2.1.7.jar
- **拼多多开放平台SDK**: 订单、商品管理
- **抖店开放平台SDK**: 订单、物流对接
- **微信开放平台SDK**: 小店订单管理
- **七牛云SDK**: 图片存储

---

## 📦 功能模块

### 1. 商品管理

#### 1.1 商品库
- **功能**: 统一管理所有商品信息
- **特性**:
  - 商品基本信息管理（编码、名称、规格）
  - SKU管理（多规格、价格、库存）
  - 商品分类管理（树形结构）
  - 品牌管理
  - 供应商管理
  - 发货方式设置（自发货/供应商发货）
- **接口**: `/api/goods/*`

#### 1.2 店铺商品
- **功能**: 管理各平台店铺的商品
- **特性**:
  - 从平台拉取店铺商品
  - 店铺商品与商品库关联
  - 一键关联商品库SKU
  - 店铺商品推送到商品库
  - 商品信息同步
- **接口**: `/api/{platform}/goods/*`

### 2. 订单管理

#### 2.1 订单库
- **功能**: 统一订单管理中心
- **特性**:
  - 多平台订单聚合查询
  - 订单状态管理
  - 订单审核发货
  - 订单取消处理
  - 订单导出
- **接口**: `/api/order/*`

#### 2.2 店铺订单
- **功能**: 各平台订单管理
- **特性**:
  - 订单API自动拉取
  - 订单增量/全量同步
  - 订单状态更新
  - 订单详情查看
  - 订单拉取日志
- **接口**: `/api/{platform}/order/*`
- **支持平台**: 
  - 淘宝天猫 (`/api/tao/order/*`)
  - 京东 (`/api/jd/order/*`)
  - 拼多多 (`/api/pdd/order/*`)
  - 抖店 (`/api/dou/order/*`)
  - 微信小店 (`/api/wei/order/*`)

### 3. 发货管理

#### 3.1 发货中心
- **功能**: 统一发货处理中心
- **特性**:
  - 待发货订单列表
  - 已发货订单查询
  - 已分配供应商发货列表
  - 批量发货操作
  - 发货方式选择
- **接口**: `/api/ship/*`

#### 3.2 发货方式
- **手动发货**: 手动录入物流信息
- **电子面单**: 自动打印快递单（开源版暂不支持）
- **供应商发货**: 分配给供应商发货

#### 3.3 备货单
- **功能**: 仓库备货管理
- **特性**:
  - 仓库备货清单
  - 供应商备货清单
  - 备货单打印
  - 出库单生成
- **接口**: `/api/shipment/*`

#### 3.4 物流管理
- **功能**: 物流跟踪管理
- **特性**:
  - 物流公司管理
  - 电子面单账户设置
  - 物流信息查询
- **接口**: `/api/logistics/*`

### 4. 售后管理

#### 4.1 售后中心
- **功能**: 统一售后管理
- **特性**:
  - 多平台售后聚合
  - 售后单状态管理
  - 售后处理记录
- **接口**: `/api/refund/*`

#### 4.2 店铺售后
- **功能**: 各平台售后处理
- **特性**:
  - 售后API自动拉取
  - 售后同意/拒绝
  - 补发/换货/退货处理
  - 售后信息同步
  - 售后拉取日志
- **接口**: `/api/{platform}/refund/*`

#### 4.3 售后处理
- **补发**: 重新发货
- **换货**: 换货流程处理
- **退货**: 退货退款处理

### 5. 库存管理

#### 5.1 库存查询
- **功能**: 实时库存监控
- **特性**:
  - 商品库存查询
  - 多仓库库存
  - 库存预警
  - 库存盘点
- **接口**: `/api/stock/inventory/*`

#### 5.2 入库管理
- **功能**: 商品入库
- **特性**:
  - 采购入库
  - 退货入库
  - 调拨入库
  - 手动入库
- **接口**: `/api/stock/in/*`

#### 5.3 出库管理
- **功能**: 商品出库
- **特性**:
  - 销售出库
  - 报损出库
  - 调拨出库
  - 手动出库
- **接口**: `/api/stock/out/*`

#### 5.4 仓库管理
- **功能**: 仓库设置
- **特性**:
  - 仓库信息管理
  - 仓位设置
  - 默认仓库设置
- **接口**: `/api/warehouse/*`

### 6. 采购管理

#### 6.1 采购订单
- **功能**: 采购管理
- **特性**:
  - 采购订单创建
  - 采购单审核
  - 采购单跟踪
- **接口**: `/api/purchase/order/*`

#### 6.2 供应商管理
- **功能**: 供应商信息管理
- **特性**:
  - 供应商基本信息
  - 供应商商品关联
  - 供应商发货设置
- **接口**: `/api/supplier/*`

### 7. 店铺管理

#### 7.1 店铺设置
- **功能**: 店铺信息管理
- **特性**:
  - 店铺基本信息
  - 平台授权配置
  - AccessToken管理
  - 店铺启用/禁用
- **接口**: `/api/shop/*`

#### 7.2 平台开关
- **功能**: 平台功能开关
- **特性**:
  - 平台启用/禁用
  - 功能开关配置
- **接口**: `/api/platform/*`

### 8. 系统管理

#### 8.1 用户管理
- **功能**: 系统用户管理
- **接口**: `/api/system/user/*`

#### 8.2 角色管理
- **功能**: 角色权限管理
- **接口**: `/api/system/role/*`

#### 8.3 菜单管理
- **功能**: 菜单权限管理
- **接口**: `/api/system/menu/*`

#### 8.4 部门管理
- **功能**: 组织架构管理
- **接口**: `/api/system/dept/*`

#### 8.5 字典管理
- **功能**: 数据字典管理
- **接口**: `/api/system/dict/*`

---

## 🔌 API接口文档

### 接口访问地址

- **Swagger UI**: http://localhost:8087/swagger-ui.html
- **API Docs**: http://localhost:8087/v3/api-docs

### 接口认证

系统使用 **JWT (JSON Web Token)** 进行身份认证。

#### 登录接口
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员"
    }
  }
}
```

#### 请求头配置
```http
Authorization: Bearer {token}
```

### 核心接口列表

#### 1. 商品管理接口

##### 1.1 获取商品列表
```http
GET /api/goods/list
Parameters:
  - pageNum: 页码（默认1）
  - pageSize: 每页数量（默认10）
  - keyword: 搜索关键词
  - categoryId: 分类ID
```

##### 1.2 创建商品
```http
POST /api/goods/add
Content-Type: application/json

{
  "goodsCode": "G001",
  "goodsName": "商品名称",
  "categoryId": 1,
  "brandId": 1,
  "supplierId": 1,
  "shipMode": 0,
  "skus": [
    {
      "skuCode": "SKU001",
      "skuName": "规格1",
      "price": 99.00,
      "costPrice": 50.00
    }
  ]
}
```

##### 1.3 更新商品
```http
PUT /api/goods/update
Content-Type: application/json

{
  "id": 1,
  "goodsName": "更新商品名称",
  "categoryId": 2
}
```

##### 1.4 删除商品
```http
DELETE /api/goods/{id}
```

#### 2. 订单管理接口

##### 2.1 拉取店铺订单
```http
POST /api/{platform}/order/pull
Content-Type: application/json

{
  "shopId": 1,
  "startTime": "2024-01-01 00:00:00",
  "endTime": "2024-01-31 23:59:59"
}
```

##### 2.2 获取订单列表
```http
GET /api/order/list
Parameters:
  - pageNum: 页码
  - pageSize: 每页数量
  - orderStatus: 订单状态
  - platform: 平台类型
  - shopId: 店铺ID
  - startTime: 开始时间
  - endTime: 结束时间
```

##### 2.3 订单审核
```http
POST /api/order/audit
Content-Type: application/json

{
  "orderIds": [1, 2, 3],
  "status": 1
}
```

#### 3. 发货管理接口

##### 3.1 手动发货
```http
POST /api/ship/manual
Content-Type: application/json

{
  "orderId": 1,
  "logisticsCompany": "顺丰速运",
  "trackingNumber": "SF1234567890",
  "shipTime": "2024-01-01 10:00:00"
}
```

##### 3.2 分配供应商发货
```http
POST /api/ship/supplier
Content-Type: application/json

{
  "orderIds": [1, 2, 3],
  "supplierId": 1
}
```

##### 3.3 获取备货单
```http
GET /api/shipment/list
Parameters:
  - type: 备货单类型（0:仓库备货，1:供应商备货）
  - status: 状态
  - startTime: 开始时间
  - endTime: 结束时间
```

#### 4. 库存管理接口

##### 4.1 查询库存
```http
GET /api/stock/inventory/list
Parameters:
  - keyword: 商品关键词
  - warehouseId: 仓库ID
```

##### 4.2 商品入库
```http
POST /api/stock/in/add
Content-Type: application/json

{
  "warehouseId": 1,
  "type": 1,
  "items": [
    {
      "goodsId": 1,
      "skuId": 1,
      "quantity": 100
    }
  ]
}
```

##### 4.3 商品出库
```http
POST /api/stock/out/add
Content-Type: application/json

{
  "warehouseId": 1,
  "type": 1,
  "items": [
    {
      "goodsId": 1,
      "skuId": 1,
      "quantity": 50
    }
  ]
}
```

#### 5. 售后管理接口

##### 5.1 拉取售后单
```http
POST /api/{platform}/refund/pull
Content-Type: application/json

{
  "shopId": 1,
  "startTime": "2024-01-01 00:00:00",
  "endTime": "2024-01-31 23:59:59"
}
```

##### 5.2 处理售后
```http
POST /api/refund/process
Content-Type: application/json

{
  "refundId": 1,
  "processType": 1,
  "logisticsCompany": "顺丰速运",
  "trackingNumber": "SF1234567890",
  "remark": "处理备注"
}
```

### 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    // 业务数据
  }
}
```

**状态码说明**:
- `200`: 成功
- `401`: 未授权
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器错误

### 分页响应格式

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 100,
    "rows": [
      // 数据列表
    ],
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 🚀 部署指南

### 环境要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| Java | 17+ | JDK环境 |
| Maven | 3.8+ | 项目构建 |
| Node.js | 16.x | 前端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |
| Nginx | 1.20+ | Web服务器 |

### 本地开发部署

#### 1. 数据库配置

##### 1.1 创建数据库
```sql
CREATE DATABASE `qihang-erp` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

##### 1.2 导入数据库结构
```bash
mysql -u root -p qihang-erp < docs/qihang-erp.sql
```

#### 2. Redis启动

```bash
# Windows
redis-server.exe

# Linux/Mac
redis-server
```

#### 3. 后端配置

##### 3.1 修改配置文件
编辑 `api/src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qihang-erp?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: your_password
    
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      # password: your_redis_password
```

##### 3.2 Maven打包
```bash
# 在项目根目录执行
mvn clean package -DskipTests
```

##### 3.3 启动后端服务
```bash
# 方式1: 使用Maven运行
cd api
mvn spring-boot:run

# 方式2: 运行jar包
java -jar api/target/api-2.2.0.jar
```

**访问地址**: http://localhost:8087

#### 4. 前端配置

##### 4.1 安装依赖
```bash
cd vue
npm install --registry=https://registry.npmmirror.com
```

##### 4.2 开发模式运行
```bash
npm run dev
```

**访问地址**: http://localhost:80

##### 4.3 生产环境打包
```bash
npm run build:prod
```

打包后的文件在 `vue/dist` 目录。

### 生产环境部署

#### 1. Docker部署（推荐）

##### 1.1 构建后端镜像
```bash
# 在api目录下
cd api
docker build -t qihangerp-api:2.2.0 .
```

##### 1.2 构建前端镜像
```bash
# 在vue目录下
cd vue
docker build -t qihangerp-vue:2.1.1 .
```

##### 1.3 Docker Compose部署
创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: qihangerp-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: qihang-erp
    volumes:
      - ./mysql-data:/var/lib/mysql
      - ./docs/qihang-erp.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - qihangerp-net

  redis:
    image: redis:7
    container_name: qihangerp-redis
    ports:
      - "6379:6379"
    networks:
      - qihangerp-net

  api:
    image: qihangerp-api:2.2.0
    container_name: qihangerp-api
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/qihang-erp?useUnicode=true&characterEncoding=utf8
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123
      SPRING_REDIS_HOST: redis
    ports:
      - "8087:8087"
    networks:
      - qihangerp-net

  nginx:
    image: nginx:1.20
    container_name: qihangerp-nginx
    depends_on:
      - api
    volumes:
      - ./vue/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
    ports:
      - "80:80"
    networks:
      - qihangerp-net

networks:
  qihangerp-net:
    driver: bridge
```

启动服务:
```bash
docker-compose up -d
```

#### 2. Nginx配置

创建 `nginx.conf`:

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # API反向代理
    location /prod-api/ {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_pass http://localhost:8087/;
    }

    # 文件上传大小限制
    client_max_body_size 50m;
}
```

#### 3. 系统配置

##### 3.1 默认登录信息
- **用户名**: admin
- **密码**: admin123

##### 3.2 首次登录配置

1. **修改管理员密码**
2. **配置店铺信息**
   - 添加店铺
   - 配置平台AppKey
   - 获取授权Token
3. **初始化商品库**
   - 创建商品分类
   - 导入商品信息
4. **拉取店铺商品**
   - 同步店铺商品
   - 关联商品库SKU

### 部署注意事项

⚠️ **安全配置**
1. 修改数据库密码
2. 修改Redis密码
3. 配置JWT密钥
4. 启用HTTPS

⚠️ **性能优化**
1. 配置Redis缓存
2. 调整数据库连接池
3. 配置Nginx缓存
4. 启用Gzip压缩

⚠️ **备份策略**
1. 定期备份数据库
2. 备份上传文件
3. 备份配置文件

---

## 📐 开发规范

### 代码规范

#### 1. 命名规范

##### 1.1 Java命名规范
- **类名**: 大驼峰命名 `GoodsController`
- **方法名**: 小驼峰命名 `getUserList`
- **变量名**: 小驼峰命名 `userName`
- **常量名**: 全大写下划线 `MAX_SIZE`
- **包名**: 全小写 `cn.qihangerp.api`

##### 1.2 数据库命名规范
- **表名**: 小写下划线 `erp_goods`
- **字段名**: 小写下划线 `goods_name`
- **索引名**: `idx_字段名`
- **唯一索引**: `uk_字段名`

##### 1.3 前端命名规范
- **组件名**: 大驼峰命名 `GoodsList.vue`
- **方法名**: 小驼峰命名 `getList`
- **变量名**: 小驼峰命名 `goodsList`
- **常量名**: 全大写下划线 `API_BASE_URL`

#### 2. 注释规范

##### 2.1 类注释
```java
/**
 * 商品管理控制器
 *
 * @author qihang
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/api/goods")
public class GoodsController {
}
```

##### 2.2 方法注释
```java
/**
 * 获取商品列表
 *
 * @param pageNum 页码
 * @param pageSize 每页数量
 * @param keyword 搜索关键词
 * @return 商品列表
 */
@GetMapping("/list")
public AjaxResult getList(Integer pageNum, Integer pageSize, String keyword) {
}
```

##### 2.3 复杂业务逻辑注释
```java
// 1. 校验商品信息
// 2. 保存商品基本信息
// 3. 保存商品SKU信息
// 4. 更新库存
```

#### 3. 代码格式

##### 3.1 缩进
- 使用4个空格缩进（不使用Tab）

##### 3.2 行宽
- 每行代码不超过120字符

##### 3.3 空行
- 方法之间空一行
- 逻辑块之间空一行

##### 3.4 导入
- 不使用通配符导入
- 按字母顺序排序

### Git提交规范

#### 1. 分支管理

- **master**: 生产环境分支
- **develop**: 开发分支
- **feature/xxx**: 功能分支
- **bugfix/xxx**: 修复分支
- **hotfix/xxx**: 紧急修复分支

#### 2. 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型 (type)**:
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档修改
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例**:
```
feat(goods): 添加商品批量导入功能

- 支持Excel导入
- 支持批量校验
- 支持导入日志查询

Closes #123
```

#### 3. 提交频率
- 每完成一个小功能提交一次
- 不要一次提交过多修改
- 每次提交确保代码可编译运行

### 接口开发规范

#### 1. RESTful API规范

- **GET**: 查询操作
- **POST**: 新增操作
- **PUT**: 修改操作
- **DELETE**: 删除操作

#### 2. URL命名
```
GET    /api/goods          # 获取列表
GET    /api/goods/{id}     # 获取详情
POST   /api/goods          # 新增
PUT    /api/goods          # 修改
DELETE /api/goods/{id}     # 删除
```

#### 3. 参数校验
```java
@PostMapping("/add")
public AjaxResult add(@Valid @RequestBody GoodsDTO goodsDTO) {
    // 业务逻辑
}
```

#### 4. 异常处理
```java
try {
    // 业务逻辑
} catch (BusinessException e) {
    return AjaxResult.error(e.getMessage());
} catch (Exception e) {
    log.error("系统异常", e);
    return AjaxResult.error("系统异常");
}
```

### 数据库设计规范

#### 1. 表设计规范
- 每个表必须有主键
- 使用自增ID作为主键
- 添加创建时间、更新时间字段
- 添加逻辑删除标识字段

#### 2. 字段设计规范
- 避免使用NULL值
- 使用合适的字段类型
- 字段长度合理设置
- 添加字段注释

#### 3. 索引设计规范
- 主键自动创建索引
- 外键字段创建索引
- 查询条件字段创建索引
- 避免过多索引

#### 4. SQL编写规范
- 避免SELECT *
- 使用PreparedStatement
- 避免在WHERE子句中使用函数
- 合理使用JOIN

### 测试规范

#### 1. 单元测试
- 每个Service方法编写单元测试
- 测试覆盖率达到80%以上
- 使用Mock隔离外部依赖

#### 2. 接口测试
- 使用Postman/Swagger测试
- 覆盖正常流程和异常流程
- 记录测试用例

#### 3. 集成测试
- 测试完整业务流程
- 测试各模块间交互
- 测试第三方接口对接

---

## ❓ 常见问题

### 1. 部署相关

#### Q1: 数据库连接失败
**问题**: `Communications link failure`

**解决**:
1. 检查MySQL是否启动
2. 检查数据库连接配置
3. 检查防火墙设置
4. 检查MySQL版本（需要8.0+）

#### Q2: Redis连接失败
**问题**: `Unable to connect to Redis`

**解决**:
1. 检查Redis是否启动
2. 检查Redis配置
3. 检查Redis密码设置

#### Q3: 前端访问404
**问题**: 页面刷新后404

**解决**:
在Nginx配置中添加:
```nginx
try_files $uri $uri/ /index.html;
```

### 2. 功能相关

#### Q4: 订单拉取失败
**问题**: 无法拉取店铺订单

**解决**:
1. 检查店铺授权是否过期
2. 检查AppKey配置是否正确
3. 检查网络连接
4. 查看拉取日志

#### Q5: 商品关联失败
**问题**: 店铺商品无法关联商品库

**解决**:
1. 确保商品库中存在对应商品
2. 检查SKU编码是否匹配
3. 使用一键关联功能

#### Q6: 发货失败
**问题**: 订单发货失败

**解决**:
1. 检查订单状态是否正确
2. 检查物流公司是否配置
3. 检查电子面单账户
4. 查看错误日志

### 3. 性能相关

#### Q7: 系统响应慢
**问题**: 接口响应时间长

**解决**:
1. 开启Redis缓存
2. 优化数据库查询
3. 增加数据库索引
4. 调整连接池大小

#### Q8: 内存占用高
**问题**: Java进程内存占用过高

**解决**:
1. 调整JVM参数
2. 检查是否有内存泄漏
3. 优化数据查询
4. 增加服务器内存

### 4. 对接相关

#### Q9: 如何获取平台AppKey
**参考文档**: [开放平台申请说明](https://mp.weixin.qq.com/s/KqyNlIVl43dTWicaAeLR1g)

**步骤**:
1. 登录对应平台开放平台
2. 创建应用
3. 获取AppKey和AppSecret
4. 配置回调地址
5. 提交审核

#### Q10: Token过期怎么办
**问题**: AccessToken过期

**解决**:
1. 重新获取授权
2. 配置自动刷新Token
3. 检查Token有效期

### 5. 开发相关

#### Q11: 如何扩展新平台
**步骤**:
1. 创建平台服务模块
2. 实现平台接口
3. 添加控制器
4. 配置路由
5. 添加前端页面

#### Q12: 如何对接外部系统
**方式**:
1. 使用开放API接口
2. 实现Webhook回调
3. 消息队列集成
4. 数据库直连（不推荐）

---

## 📞 技术支持

### 联系方式

- **GitHub**: https://github.com/qiliping/qihangerp
- **Gitee**: https://gitee.com/qiliping/qihangerp
- **微信公众号**: 启航电商技术
- **问题反馈**: 请提交 [Issue](https://github.com/qiliping/qihangerp/issues)

### 获取帮助

1. **查看文档**: 先查看本Wiki文档
2. **搜索Issue**: 搜索是否有相同问题
3. **提交Issue**: 详细描述问题和环境
4. **加入社区**: 关注微信公众号加入交流群

### 贡献代码

欢迎提交 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/xxx`)
3. 提交改动 (`git commit -m 'feat: add xxx'`)
4. 推送分支 (`git push origin feature/xxx`)
5. 创建 Pull Request

---

## 📄 许可证

本项目采用 [AGPL-3.0](../LICENSE) 开源协议。

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

**您的 Star ⭐ 是我们前进的动力！**

---

**文档版本**: v1.0  
**最后更新**: 2026-02-12  
**维护者**: 启航科技团队
