<template>
  <div style="height: 97vh; display: flex; align-items: center; justify-content: center; background-color: #3498db">
    <div style="display: flex; background-color: white; width: 25%; border-radius: 5px; overflow: hidden; padding: 10px;">
      <div style="flex: 1; display: flex; align-items: center; justify-content: center">
        <el-form :model="user" style="width: 80%" :rules="rules" ref="loginRef">
          <div style="font-size: 20px; font-weight: bold; text-align: center; margin-bottom: 20px">学习分析数据标注系统登录</div>
          <el-form-item prop="username">
            <el-input prefix-icon="el-icon-user" size="medium" placeholder="请输入账号" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="请输入密码" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%; background-color: #3498db;" @click="login">登 录</el-button>
          </el-form-item>
          <div style="display: flex">
            <div style="flex: 1; font-size: 13px;">还没有账号？请 <span style="color: #3498db; cursor: pointer" @click="$router.push('/register')">注册</span></div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      user: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ]
      }
    }
  },
  methods: {
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.$request.post('/web/login', this.user).then(res => {
            if (res.code === '200') {
              this.$router.push('/annotation2')
              this.$message.success('登录成功')
              localStorage.setItem("user", JSON.stringify(res.data))  // 存储用户数据
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
</style>