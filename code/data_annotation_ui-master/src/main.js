import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vcolorpicker from 'vcolorpicker'


import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

import request from "@/utils/request";

import VideoPlayer from 'vue-video-player'
import 'vue-video-player/src/custom-theme.css'
import 'video.js/dist/video-js.css'


import VueSlider from 'vue-slider-component';
import 'vue-slider-component/theme/default.css';

import fabric from 'fabric';

import * as echarts from 'echarts';
Vue.prototype.$echarts = echarts;

Vue.use(vcolorpicker)
Vue.use(fabric);

Vue.component('VueSlider', VueSlider);

Vue.use(ElementUI, { size: "mini" });

Vue.use(VideoPlayer);

Vue.config.productionTip = false

Vue.prototype.$request=request
// Vue.prototype.$baseUrl=process.env.VUE_APP_BASEURL

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')