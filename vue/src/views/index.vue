<template>
  <div class="chat-home-container">
    <!-- 左侧聊天区域 -->
    <div class="chat-container">
      <div class="chat-header">
        <div class="header-left">
          <i class="el-icon-chat-dot-round"></i>
          <span class="title">工作助手</span>
        </div>
        <div class="header-right">
          <el-tag type="success" size="mini">在线</el-tag>
        </div>
      </div>
      
      <div class="chat-messages" ref="messageContainer">
        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          :class="['message-item', message.isMe ? 'message-right' : 'message-left']"
        >
          <div class="message-avatar">
            <el-avatar :size="32" :src="message.avatar">
              {{ message.isMe ? '我' : '助' }}
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>
      
      <div class="chat-input">
        <el-input
          type="textarea"
          :rows="3"
          placeholder="请输入消息..."
          v-model="inputMessage"
          @keyup.enter.native="sendMessage"
        ></el-input>
        <div class="input-actions">
          <el-button type="primary" size="small" @click="sendMessage" :disabled="!inputMessage.trim()">
            发送
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 右侧导航面板 -->
    <div class="nav-panel">
      <div class="nav-header">
        <i class="el-icon-menu"></i>
        <span class="title">快捷导航</span>
      </div>
      
      <!-- 快捷功能区 -->
      <div class="quick-actions">
        <div class="section-title">
          <i class="el-icon-lightning"></i>
          快捷操作
        </div>
        <div class="action-grid">
          <div 
            v-for="action in quickActions" 
            :key="action.id"
            class="action-item"
            @click="sendQuickMessage(action)"
          >
            <div class="action-icon">
              <i :class="action.icon"></i>
            </div>
            <div class="action-text">{{ action.title }}</div>
          </div>
        </div>
      </div>
      
      <!-- 常用功能区 -->
      <div class="common-functions">
        <div class="section-title">
          <i class="el-icon-star-on"></i>
          常用功能
        </div>
        <div class="function-list">
          <div 
            v-for="func in commonFunctions" 
            :key="func.id"
            class="function-item"
            @click="navigateTo(func.path)"
          >
            <i :class="func.icon"></i>
            <span>{{ func.title }}</span>
          </div>
        </div>
      </div>
      
      <!-- 系统统计 -->
      <div class="system-stats">
        <div class="section-title">
          <i class="el-icon-data-analysis"></i>
          系统概览
        </div>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">{{ stats.waitShip }}</div>
            <div class="stat-label">待发货</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">今日订单</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ stats.salesVolume }}</div>
            <div class="stat-label">今日销售额</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.shopCount }}</div>
            <div class="stat-label">店铺数</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { todayDaily } from "@/api/report/report";
import { getToken } from "@/utils/auth";

export default {
  name: 'Index',
  data() {
    return {
      inputMessage: '',
      messages: [
        {
          content: '您好！我是您的工作助手，有什么可以帮助您的吗？',
          time: this.formatTime(new Date()),
          isMe: false,
          avatar: ''
        }
      ],
      quickActions: [
        { id: 1, title: '查看待发货', icon: 'el-icon-truck', message: '请帮我查看今天有多少待发货订单' },
        { id: 2, title: '同步订单', icon: 'el-icon-refresh', message: '请同步所有店铺的订单数据' },
        { id: 3, title: '库存预警', icon: 'el-icon-warning', message: '检查一下哪些商品库存不足' },
        { id: 4, title: '今日统计', icon: 'el-icon-data-line', message: '请告诉我今天的销售统计数据' },
        { id: 5, title: '商品管理', icon: 'el-icon-goods', message: '我想管理商品信息' },
        { id: 6, title: '售后处理', icon: 'el-icon-service', message: '查看需要处理的售后订单' }
      ],
      commonFunctions: [
        { id: 1, title: '订单管理', icon: 'el-icon-s-order', path: '/order' },
        { id: 2, title: '商品管理', icon: 'el-icon-goods', path: '/goods' },
        { id: 3, title: '库存管理', icon: 'el-icon-box', path: '/stock' },
        { id: 4, title: '发货管理', icon: 'el-icon-truck', path: '/shipping' },
        { id: 5, title: '售后管理', icon: 'el-icon-service', path: '/refund' },
        { id: 6, title: '店铺管理', icon: 'el-icon-shop', path: '/shop' },
        { id: 7, title: '采购管理', icon: 'el-icon-shopping-cart-full', path: '/purchase' },
        { id: 8, title: '系统设置', icon: 'el-icon-setting', path: '/system' }
      ],
      stats: {
        waitShip: 0,
        salesVolume: 0,
        orderCount: 0,
        shopCount: 0
      },
      sse: null,
      clientId: '',
      isSseConnected: false,
      isLoading: false
    }
  },
  mounted() {
    this.loadSystemStats();
    this.initSse();
  },
  beforeDestroy() {
    this.closeSse();
  },
  methods: {
    initSse() {
      // 生成唯一客户端ID
      this.clientId = 'client_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      // 获取token
      const token = getToken();
      
      // 建立SSE连接，携带token
      this.sse = new EventSource(`${process.env.VUE_APP_BASE_API}/api/sse/connect?clientId=${this.clientId}&token=${token}`);
      
      // 监听连接成功
      this.sse.addEventListener('connected', (event) => {
        console.log('SSE连接成功:', event.data);
        this.isSseConnected = true;
      });
      
      // 监听消息
      this.sse.addEventListener('message', (event) => {
        console.log('收到SSE消息:', event.data);
        // 移除正在思考的消息
        if (this.isLoading) {
          this.messages = this.messages.filter(msg => !msg.isLoading);
          this.isLoading = false;
        }
        // 添加实际回复消息
        this.messages.push({
          content: event.data,
          time: this.formatTime(new Date()),
          isMe: false,
          avatar: ''
        });
        this.scrollToBottom();
      });
      
      // 监听心跳
      this.sse.addEventListener('heartbeat', (event) => {
        console.log('收到心跳:', event.data);
      });
      
      // 监听错误
      this.sse.onerror = (error) => {
        console.error('SSE连接错误:', error);
        this.isSseConnected = false;
        // 尝试重连
        setTimeout(() => {
          this.initSse();
        }, 5000);
      };
    },
    
    closeSse() {
      if (this.sse) {
        this.sse.close();
        this.sse = null;
      }
    },
    
    sendMessage() {
      if (!this.inputMessage.trim()) return;
      
      // 添加用户消息
      this.messages.push({
        content: this.inputMessage,
        time: this.formatTime(new Date()),
        isMe: true,
        avatar: ''
      });
      
      // 显示正在思考的loading效果
      this.isLoading = true;
      this.messages.push({
        content: '正在思考...',
        time: this.formatTime(new Date()),
        isMe: false,
        avatar: '',
        isLoading: true
      });
      
      // 获取token
      const token = getToken();
      
      // 通过SSE发送消息到后端
      if (this.isSseConnected) {
        // 使用fetch发送消息
        fetch(`${process.env.VUE_APP_BASE_API}/api/sse/send?clientId=${this.clientId}&message=${encodeURIComponent(this.inputMessage)}&token=${token}`)
          .then(response => response.text())
          .then(data => {
            console.log('消息发送结果:', data);
          })
          .catch(error => {
            console.error('消息发送失败:', error);
            // 发送失败时使用模拟回复
            this.generateReply(this.inputMessage);
            this.isLoading = false;
          });
      } else {
        // SSE未连接时使用模拟回复
        this.generateReply(this.inputMessage);
        this.isLoading = false;
      }
      
      this.inputMessage = '';
      this.scrollToBottom();
    },
    
    sendQuickMessage(action) {
      this.inputMessage = action.message;
      this.sendMessage();
    },
    
    generateReply(userMessage) {
      let reply = '';
      
      if (userMessage.includes('待发货')) {
        reply = `您有 ${this.stats.waitShip} 个待发货订单，建议尽快处理。`;
      } else if (userMessage.includes('订单')) {
        reply = `今日订单总数：${this.stats.orderCount} 单，销售额：¥${this.stats.salesVolume}`;
      } else if (userMessage.includes('库存')) {
        reply = '正在为您检查库存情况，请稍等...';
      } else if (userMessage.includes('统计')) {
        reply = `今日数据：订单${this.stats.orderCount}单，销售额¥${this.stats.salesVolume}，待发货${this.stats.waitShip}单`;
      } else {
        reply = '我理解您的需求，正在为您处理...';
      }
      
      this.messages.push({
        content: reply,
        time: this.formatTime(new Date()),
        isMe: false,
        avatar: ''
      });
      
      this.scrollToBottom();
    },
    
    navigateTo(path) {
      this.$router.push(path);
    },
    
    loadSystemStats() {
      todayDaily().then(resp => {
        this.stats = resp.data;
      }).catch(error => {
        console.error('加载统计失败:', error);
      });
    },
    
    formatTime(date) {
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messageContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-home-container {
  display: flex;
  height: calc(100vh - 84px);
  background-color: #f5f7fa;
  padding: 20px;
  gap: 20px;
}

// 左侧聊天区域
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  background: #fafafa;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon-chat-dot-round {
      font-size: 20px;
      color: #409eff;
    }
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fafafa;
  
  .message-item {
    display: flex;
    margin-bottom: 20px;
    max-width: 80%;
    
    &.message-left {
      align-self: flex-start;
    }
    
    &.message-right {
      align-self: flex-end;
      flex-direction: row-reverse;
      
      .message-content {
        background: #409eff;
        color: white;
        .message-time {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }
  
  .message-avatar {
    margin: 0 12px;
  }
  
  .message-content {
    background: white;
    padding: 12px 16px;
    border-radius: 18px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    
    .message-text {
      font-size: 14px;
      line-height: 1.5;
      margin-bottom: 4px;
    }
    
    .message-time {
      font-size: 12px;
      color: #999;
      text-align: right;
    }
  }
}

.chat-input {
  padding: 20px;
  border-top: 1px solid #eee;
  background: white;
  
  .input-actions {
    margin-top: 12px;
    text-align: right;
  }
}

// 右侧导航面板
.nav-panel {
  width: 320px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow-y: auto;
}

.nav-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  background: #fafafa;
  
  .el-icon-menu {
    font-size: 18px;
    color: #409eff;
  }
  
  .title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px 8px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

// 快捷操作
.quick-actions {
  .action-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 0 20px 20px;
  }
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px;
    border: 1px solid #eee;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
      background: #f0f8ff;
      transform: translateY(-2px);
    }
    
    .action-icon {
      font-size: 24px;
      color: #409eff;
      margin-bottom: 8px;
    }
    
    .action-text {
      font-size: 13px;
      color: #666;
    }
  }
}

// 常用功能
.common-functions {
  .function-list {
    padding: 0 20px 20px;
  }
  
  .function-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 8px;
    
    &:hover {
      background: #f0f8ff;
      color: #409eff;
    }
    
    i {
      font-size: 16px;
      width: 20px;
    }
    
    span {
      font-size: 14px;
    }
  }
}

// 系统统计
.system-stats {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 0 20px 20px;
  }
  
  .stat-item {
    text-align: center;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;
    
    .stat-value {
      font-size: 20px;
      font-weight: 600;
      color: #409eff;
      margin-bottom: 4px;
    }
    
    .stat-label {
      font-size: 12px;
      color: #999;
    }
  }
}

// 滚动条样式
.chat-messages::-webkit-scrollbar,
.nav-panel::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track,
.nav-panel::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb,
.nav-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.nav-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
