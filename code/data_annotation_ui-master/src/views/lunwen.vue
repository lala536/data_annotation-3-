<template>
  <div class="container">
    <div class="row">
      <div class="col" style="border: none;">分析详情</div>
    </div>
    <div class="row">
      <div class="col">
        <br><br><br>
        姓名：屈**<br><br><br>
        学号：12<br><br><br>
        学习投入：高（高/中/低）
      </div>
      <div class="col">
        <div
          ref="echart1"
          id="echart1"
          style="width: 500px; height: 300px;"
        ></div>
      </div>
      <div class="col">
        <div
          ref="echart2"
          id="echart2"
          style="width: 500px; height: 300px;"
        ></div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div id="echart3" style="width: 1300px;height:300px;"></div>
      </div>
    </div>
    
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default {
  data() {
    return {
    }
  },
  mounted() {
    this.c1()
    this.c2()
    this.c3()
  },
  methods: {
    c1() {
      const option = {
        series: [
          {
            type: 'pie',
            data: [
              {
                value: 572+174,
                name: '高',
                itemStyle: {
                  color: '#67C23A'
                }
              },
              {
                value: 740+2884,
                name: '中',
                itemStyle: {
                  color: '#E6A23C'
                }
              },
              {
                value: 195+525,
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
          data: ['笔记', '讨论', '听讲', '答题', '玩手机', '其他']
        },
        yAxis: {},
        series: [
          {
            type: 'bar',
            data: [740,572,2884,174,195,525],
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
    },
    c3() {
      this.$request.get("/a", {
        
      }).then((res) => {
        const array = res.data[0].split(', ').map(Number).map(element => element - 1601281476)
        const array2 = res.data[1].split(', ').map(element => element.replace(/^'|'$/g, '')).map(element => {
          return element.trim() === '睡觉' ? '其他' : element.trim();
        })

        var echart3 = echarts.init(document.getElementById('echart3'))
        let option = {
          xAxis: {
            type: 'category',
            data: array,
          },
          yAxis: {
            type: 'category',
            data: ['笔记', '讨论', '听讲', '答题', '玩手机', '其他']
          },
          series: [
            {
              data: array2,
              type: 'line'
            }
          ]
        };
        echart3.setOption(option);// 渲染页面
      })
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
  flex: 1;
  /* border: 1px solid black; */
  padding: 10px;
  margin: 5px;
}
</style>