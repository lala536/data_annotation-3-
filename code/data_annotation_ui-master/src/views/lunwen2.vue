<template>
  <div class="container">
    <div class="row">
      <div class="col" style="border: none;">课堂分析结果</div>
    </div>
    <div class="row">
      <div class="col" style="height: 640px; width: 800px; overflow: auto;">
        <el-card :body-style="{ padding: '0px', textAlign: 'center' }" v-for="(student, index) in students" :key="index" class="student-card">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="-1 -1 26 26" width="110" height="110" fill="none" :stroke="student.color" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <circle cx="8" cy="10" r="0.7" />
            <circle cx="16" cy="10" r="0.7" />
            <path d="M7 16s5 5 10 0" />
          </svg><br>
          {{ student.number }}
          {{ student.name }}
        </el-card>
      </div>
      <div class="col" style="width: 500px;">
        <div class="row">
          <div
            ref="echart1"
            id="echart1"
            style="width: 500px; height: 300px;"
          ></div>
        </div>
        <div class="row">
          <div
            ref="echart2"
            id="echart2"
            style="width: 500px; height: 300px;"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default {
  data() {
    return {
      students: [],
      name: [
    '党**',
    '冯**',
    '刘**',
    '刘**',
    '刘**',
    '刘**',
    '周**',
    '夏**',
    '奚**',
    '孙**',
    '尹*',
    '屈**',
    '屈**',
    '张**',
    '徐**',
    '徐**',
    '李**',
    '梁**',
    '欧**',
    '毛*',
    '温**',
    '王**',
    '罗**',
    '肖**',
    '胡*',
    '胡**',
    '苟**',
    '谢*',
    '谯*',
    '贺**',
    '陈**',
    '陈*',
    '陈*',
    '陈**',
    '韩**',
    '高**',
    '黄**'
],
      color: ['#67C23A','#F56C6C','#F56C6C','#F56C6C',
              '#E6A23C','#E6A23C','#E6A23C','#E6A23C',
              '#F56C6C','#67C23A','#67C23A','#67C23A',
              '#67C23A','#F56C6C','#F56C6C','#F56C6C',
              '#F56C6C','#E6A23C','#E6A23C','#67C23A',
              '#F56C6C','#67C23A','#67C23A','#67C23A',
              '#67C23A','#F56C6C','#E6A23C','#E6A23C',
              '#F56C6C','#E6A23C','#E6A23C','#E6A23C',
              '#E6A23C','#67C23A','#E6A23C','#67C23A'],
    }
  },
  mounted() {
    this.c1()
    this.c2()
    for (let i = 1; i <= 36; i++) {
      this.students.push({ number: `编号${i}`, name: this.name[i-1], color: this.color[i-1]});
    }
  },
  methods: {
    c1() {
      const option = {
        series: [
          {
            type: 'pie',
            data: [
              {
                value: 12,
                name: '高',
                itemStyle: {
                  color: '#67C23A'
                }
              },
              {
                value: 13,
                name: '中',
                itemStyle: {
                  color: '#E6A23C'
                }
              },
              {
                value: 11,
                name: '低',
                itemStyle: {
                  color: '#F56C6C'
                }
              }
            ]
          }
        ]
      };
      const echart1 = echarts.init(this.$refs.echart1);// 图标初始化
      echart1.setOption(option);// 渲染页面
    },
    c2() {
      const option = {
        xAxis: {
          data: ['高','中','低']
        },
        yAxis: {},
        series: [
          {
            type: 'bar',
            data: [5, 3, 7],
            label: {
                show: true,
                position: 'top',
                formatter: '{c}' // 显示数据值
            }
          },
          {
            type: 'bar',
            data: [7,10,4],
            label: {
                show: true,
                position: 'top',
                formatter: '{c}' // 显示数据值
            }
          }
        ]
      };
      const echart2 = echarts.init(this.$refs.echart2);// 图标初始化
      echart2.setOption(option);// 渲染页面
    }
  }
}
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  border: 1px solid black;
}

.row {
  display: flex;
}

.col {
  border: 1px solid black;
  padding: 10px;
  margin: 5px;
}

.student-card {
  width: calc(25% - 15px); /* 每行4列，减去间距 */
  height: 146px;
  margin: 5px;
  display: inline-block; /* 使用 display: inline-block; 替代 float: left; */
  overflow: auto; /* 内容过长时显示滚动条 */
}
</style>