<template>
  <div>
    <el-container>
      <!-- 头部 -->
      <el-header style="height: 3vh;">
        <el-menu router :default-active="$route.path" class="el-menu-demo" mode="horizontal" style="height: 3vh;">
          <el-menu-item style="height: 3vh; line-height: 3vh;" index="/annotation2">视频标注</el-menu-item>
          <el-menu-item style="height: 3vh; line-height: 3vh;" index="" @click.native="logout">退出登录</el-menu-item>
        </el-menu>
      </el-header>

      <el-container>
        <el-main>
          <router-view @update:user="updateUser" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
    }
  },
  mounted() {   // 页面加载完成之后触发
    if (!this.user.id) {   // 当前的浏览器没有用户信息
      this.$router.push('/login')
    }
  },
  methods: {
    updateUser(user) {   // 获取子组件传过来的数据  更新当前页面的数据
      this.user = JSON.parse(JSON.stringify(user))  // 让父级的对象跟子级的对象毫无关联
    },
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    },
  },
}
</script>

<style scoped>

</style>