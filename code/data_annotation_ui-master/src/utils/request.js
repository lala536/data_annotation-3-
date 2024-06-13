import axios from 'axios'

// 创建可一个新的axios对象
const request = axios.create({
    baseURL: 'http://localhost:8888',   // 后端的接口地址
    // baseURL: 'http://10.131.102.150:8888',   // 后端的接口地址
    timeout: 50000
})

// request 拦截器
request.interceptors.request.use(config => {
        config.headers['Content-Type'] = 'application/json;charset=utf-8'
        let user = JSON.parse(localStorage.getItem("user") || '{}')
        config.headers['token'] = user.token  // 设置请求头
        return config
    }, error => {
        console.error('request error: ' + error)
        return Promise.reject(error)
    }
)

// response 拦截器
request.interceptors.response.use(

    response => {
        let res = response.data
        // if (typeof res === 'string') {
        //     res = res ? JSON.parse(res) : res
        // }
        if (res.code === '401') {
            router.push('/login')
        }
        return res
    },
    error => {
        console.error('response error: ' + error)
        return Promise.reject(error)
    },
)

export default request