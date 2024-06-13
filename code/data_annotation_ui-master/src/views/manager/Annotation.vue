<template>
  <div> 
    <el-container style="height: 89vh">
      <!-- 数据标注部分 -->
      <el-main style="box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); width: 66%">
        <!-- 顶部选项 -->
        <div style="margin-bottom: 10px;">
          <!-- 选择视频按钮 -->
          <el-button type="primary" plain @click="getVideo">视频</el-button>
          <!-- 选择标签组按钮 -->
          <el-button type="primary" plain @click="getLabelType">标签组</el-button>
          <!-- 画框 -->
          <el-button type="primary" plain @click="startDraw">{{ startDrawMode ? '结束' : '画框' }}</el-button>
          
          <el-link style="margin-left: 25px;" :underline="false">被标注者：</el-link>
          <!-- 选择对应被标注者（本质上是选择person_id）用于画框和打标签 -->
          <el-select v-model="selectedPersonId" @change="showRecord" clearable :disabled="isSelectedPersonIdDisabled" style="width: 100px;">
            <el-option
              v-for="(name, personId) in personIdNameMap"
              :key="parseInt(personId)"
              :label="name"
              :value="parseInt(personId)">
            </el-option>
          </el-select>

          <el-link style="margin-left: 25px;" :underline="false">矩形框：</el-link>
          <!-- 根据person_id查询Rect -->
          <el-select v-model="searchRectPersonId" @change="showRect" clearable :disabled="isSearchDisabled" style="width: 100px;">
            <el-option
              :label="'全选'"
              :value="'全选'">
            </el-option>
            <el-option
              v-for="(name, personId) in personIdNameMap"
              :key="parseInt(personId)"
              :label="name"
              :value="parseInt(personId)">
            </el-option>
          </el-select>

          <el-link v-show="showSelectAnnotator" style="margin-left: 25px;" :underline="false">标注者：</el-link>
          <!-- 根据uid查询Record和Rect -->
          <el-select v-show="showSelectAnnotator" v-model="selectedUsername" :disabled="isSearchDisabled" @change="showRecordAndRectByUid" style="width: 100px;">
            <el-option
              v-for="username in usernames"
              :key="username"
              :label="username"
              :value="username">
            </el-option>
          </el-select>

          <!-- 导出 -->
          <el-button type="primary" plain @click="exportRecord" style="margin-left: 10px;">导出</el-button>

          <!-- 选择视频 -->
          <el-drawer title="视频列表" direction="ltr" :visible.sync="videoDrawerVisible" size="50%">
            <el-tree
              :data="treeData"
              :props="treeProps"
              node-key="id"
              ref="videoTree"
              show-checkbox
              highlight-current
            ></el-tree>
            <el-button style="margin-left: 20px; margin-top: 20px;" type="primary" @click="chooseVideo">选 择</el-button>
          </el-drawer>

          <!-- 选择标签组 -->
          <el-dialog title="标签组" :visible.sync="labelTypeDialogVisible" width="30%" @close="closeLabelTypes">
            <el-radio-group v-model="checkedLabelType">
              <el-radio v-for="labelType in labelTypes" :label="labelType" :key="labelType">{{ labelType }}</el-radio>
            </el-radio-group>
            <div slot="footer" class="dialog-footer">
              <el-button @click="closeLabelTypes">取 消</el-button>
              <el-button type="primary" @click="chooseLabelTypes">选 择</el-button>
            </div>
          </el-dialog>
        </div>

        <!-- 播放器和画布 -->
        <div style="position: relative;">
          <!-- 播放器 -->
          <div style="z-index: 1;">
            <video-player class="video-player vjs-custom-skin" 
              ref="videoPlayer" 
              :playsinline="true" 
              :options="playerOptions"
              @timeupdate="handleTimeUpdate"
              @pause="handlePause"
              @play="handlePlay"
              @canplay="addVideoOperationListener">
            </video-player>
          </div>
          <!-- 画布 -->
          <div class="canvas-container">
            <canvas id="canvas"></canvas>
          </div>
        </div>

        <!-- 进度条 -->
        <div style="margin-top: 5px;">
          <vue-slider
            :dotSize="0.5"
            :dotStyle="{width: '0.1px', height: '26px', backgroundColor: 'black', borderRadius: '0', transform: 'translateY(-13px)'}"
            :railStyle="{height: '20px', borderRadius: '0'}" 
            :processStyle="{borderRadius: '0'}"
            :process="generateProcess"
            :min="0"
            :max="maxProgress"
            :interval="1"
            :tooltip-formatter="formatTooltip"
            :tooltip-placement="'bottom'"
            :drag-on-click="true"
            :disabled="startDrawMode"
            v-model="progress"
            ref="slider"
            @change="handleSliderChange">
          </vue-slider>
          <div v-show="showMark" :style="'width: 1px; height: 26px; background-color: black; transform: translateX(' + translateXValue + 'px) translateY(-7.5px); position: relative; z-index: 1 !important;'"></div>
        </div>

        <!-- 打标签按钮 -->
        <div style="position: absolute; bottom: 80px; left: 48px;">
          <el-button v-for="item in this.buttonColors[this.finalCheckedLabelType]" :key="item" @click="annotation(item.label)" :style="{ backgroundColor:item.color }">{{ item.label }}</el-button>
          <el-button v-show="this.finalCheckedLabelType" @click="cancelAnnotation()">取消</el-button>
        </div>
      </el-main>

      <!-- 校验数据展示部分 -->
      <el-main style="box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); width: 34%">
        <!-- 表格 -->
        <el-table :data="recordTableData" height="618" :default-sort = "{prop: 'start', order: 'ascending'}" border :highlight-current-row="true" style="width: 100%;">
          <el-table-column min-width="26%" prop="start" label="开始" sortable>
            <template slot-scope="scope">
              <span v-if="isEditingRow !== scope.$index"
                @click="jumpToTime(scope.row.start)"
                style="cursor: pointer">{{ scope.row.start }}</span>
              <el-input v-else v-model="scope.row.start" size="mini"></el-input>
            </template>
          </el-table-column>
          <el-table-column min-width="26%" prop="end" label="结束" sortable>
            <template slot-scope="scope">
              <span v-if="isEditingRow !== scope.$index"
                @click="jumpToTime(scope.row.end)"
                style="cursor: pointer">{{ scope.row.end }}</span>
              <el-input v-else v-model="scope.row.end" size="mini"></el-input>
            </template>
          </el-table-column>
          <el-table-column min-width="24%" prop="label" label="label">
            <template slot-scope="scope">
              <span v-if="isEditingRow !== scope.$index" 
              :style="{color: getLabelColor(scope.row.label)}">
                {{scope.row.label}}
              </span>
              <el-select v-else v-model="scope.row.label" size="mini">
                <el-option v-for="label in buttonColors[finalCheckedLabelType].map((item) => item.label)" :key="label" :label="label" :value="label"></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column min-width="12%" label="修改">
            <template slot-scope="scope">
              <el-button size="mini" type="text" class="is-text1" @click="updateRecord(scope.$index, scope.row)">修改</el-button>
            </template>
          </el-table-column>
          <el-table-column min-width="12%" label="删除">
            <template slot-scope="scope">
              <el-button size="mini" type="text" class="is-text2" @click="deleteRecord(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),  // 获取当前登录的user信息
      treeData: [],  // 这里存放构建好的树形数据
      treeProps: {
        label: 'label',  // 每个节点显示的文本
        children: 'children'  // 子节点数组的字段名
      },
      labelData: [],  // 这里存放所有的label数据
      labelTypes: [],  // 这里存放所有的labelType数据
      buttonColors: [],  // 存储按钮颜色的数组
      labelTypeMap: [],  // label到labelType的映射
      videoDrawerVisible: false,  // 控制选择视频页面显示
      selectedNodes: null,  // 使用复选框获取选中的节点
      videoPath: null,  // 视频路径
      videoName: null,  // 视频名称
      labelTypeDialogVisible: false,  // 控制选择标签组页面显示
      checkedLabelType: null,  // 选中的标签组
      finalCheckedLabelType: null,  // 点击选择完成后的最终选择标签
      personIds: [],  // 该视频的所有person_id
      personIdNameMap: {}, // person_id和名字的映射
      selectedPersonId: null,  // 用于画框和标记
      searchRectPersonId: null,  // 用于查询Rect
      usernames: [],  // 所有username
      showSelectAnnotator: false,  // 是否显示根据uid查询信息
      selectedUsername: null,  // 选择的user的username
      recordTableData: [],  // 存储Record信息
      recordSegments: [],  // 存储Record数组
      progress: 0,  // 进度
      maxProgress: 0,  // 最大进度
      showMark: false,  // 打标签的开始标记
      translateXValue: 0,  // 一个偏移量
      annotationInProgress: null,  // 用于存储当前正在标注的数据
      currentAnnotationLabel: null,  // 用于存储当前标注的标签
      isEditingRow: null,  // 是否正在修改
      firstStart: null,  // 初始的开始时间
      firstEnd: null,  // 初始的结束时间
      firstLabel: null,  // 初始的标签
      isPaused: false,  // 是否暂停
      canvas: null,  // 画布
      startDrawMode: false,  // 开启画框模式
      isDrawing: false,  // 正在画框
      startPoint: null,  // 鼠标起始点
      endPoint: null,  // 鼠标结束点
      rects: {},  // 保存每个person_id对应的矩形框，用于发送数据到数据库
      faces: [],  // 用于从数据库中获取每一条face数据
      isSearchDisabled: false,  // 是否允许操作下拉框
      isSelectedPersonIdDisabled: false,
      currentTime: null,  // 当前时间，单位是秒
      duration: null,  // 总时长，单位是秒
      playerOptions: {  // 视频播放器选项
        playbackRates: [0.5, 1.0, 1.5, 2.0],  // 可选的播放速度
        autoplay: true,  // 自动播放
        muted: false,     // 默认静音
        loop: false,      // 循环播放
        preload: 'auto',  // 预先加载视频
        language: 'zh-CN',
        aspectRatio: '16:9',
        fluid: true,  // 自适应其容器。
        sources: [{
          type: "video/mp4",  // 类型
          src: ""   // url
        }],
        poster: '',  // 封面地址
        notSupportedMessage: '请选择视频',
        controlBar: {
          timeDivider: true,           // 当前时间和持续时间的分隔符
          durationDisplay: true,       // 显示持续时间
          remainingTimeDisplay: false,  // 是否显示剩余时间功能
          fullscreenToggle: false,      // 是否显示全屏按钮
          currentTimeDisplay: true,    // 当前时间
          volumeControl: false,         // 声音控制键
          playToggle: true,            // 暂停和播放键
          progressControl: false        // 进度条
        }
      },
    }
  },
  async created() {
    await this.loadVideoTree()
    await this.loadLabel()
    await this.loadUsername()
    if (this.user.role === "管理员") {
      this.showSelectAnnotator = true
      this.selectedUsername = this.user.username
    }
  },
  mounted() {
    // 创建 Fabric.js 画布实例
    this.canvas = new fabric.Canvas('canvas', {
      backgroundColor: 'rgba(0, 0, 0, 0)',
      width: this.$refs.videoPlayer.$el.offsetWidth,
      height: this.$refs.videoPlayer.$el.offsetHeight
    })
  },
  methods: {
    async loadVideoTree() {
      // 首先获取所有class、group、video表的数据
      const courseRes = await this.$request.get("/class/getAllClass")
      const groupRes = await this.$request.get("/group/getAllGroup")
      const videoRes = await this.$request.get("/video/getAllVideo")

      // 转化为数组
      const courseData = Object.values(courseRes.data)
      const groupData = Object.values(groupRes.data)
      const videoData = Object.values(videoRes.data)

      // 构建树形数据
      const treeData = []

      // 遍历class信息
      courseData.forEach((course) => {
        // 创建courseNode树节点
        const courseNode = {
          id: course.id,
          label: course.className,
          children: [],
        }
        
        // 遍历group信息
        groupData.forEach((group) => {
          // 如果小组的classId与上一级遍历的course.id相等
          if (group.classId === course.id) {
            // 创建groupNode树节点
            const groupNode = {
              id: group.id,
              label: group.groupName,
              children: [],
              parent: courseNode  // 父节点
            }
            
            // 存储视频前缀，取出类似192.168.0.101的前缀存放在videoPrefixes
            const videoPrefixes = {}

            // 遍历video信息
            videoData.forEach((video) => {
              // 如果视频的classId与最外层遍历的course.id相等并且视频的groupId与上一级遍历的group.id相等
              if (video.classId === course.id && video.groupId === group.id) {
                // 根据video.videoName获取前缀
                const videoPrefix = video.videoName.split('_')[0]

                // 检查该前缀是否已经存在，如果不存在则创建节点
                if (!videoPrefixes[videoPrefix]) {
                  videoPrefixes[videoPrefix] = {
                    id: videoPrefix,
                    label: videoPrefix,
                    children: [],
                    parent: groupNode,  // 父节点
                  }
                }

                // 在前缀节点下添加视频节点
                videoPrefixes[videoPrefix].children.push({
                  id: video.id,
                  label: video.videoName,
                  videoPath: video.videoPath,  // 添加一条视频路径信息以供后续代码获取
                  parent: videoPrefixes[videoPrefix]  // 保留父节点
                })
              }
            })
            
            // 将前缀节点添加到相应的组节点下
            Object.values(videoPrefixes).forEach((prefixNode) => {
              groupNode.children.push(prefixNode)
            })
            courseNode.children.push(groupNode)
          }
        })
        treeData.push(courseNode)
      })
      this.treeData = treeData
    },
    async loadLabel() {
      // 获取所有label数据
      const res = await this.$request.get("/label/getAllLabel", {
        params: {
          isGroup: 0,
        }
      })
      this.labelData = res.data

      // 过滤出labelTypes
      this.labelTypes = [...new Set(this.labelData.map((item) => item.labelType))]

      // 初始化buttonColors
      this.labelData.forEach((item) => {
        const { labelType, label, color } = item
        if (!this.buttonColors[labelType]) {
          this.buttonColors[labelType] = []
        }
        this.buttonColors[labelType].push({ label, color })

        // 创建label到labelType的映射
        this.labelTypeMap[label] = labelType
      })
    },
    async loadUsername() {
      // 获取所有username
      const res = await this.$request.get("/user/getUsernames")
      this.usernames = res.data
    },
    getVideo() {
      if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.videoDrawerVisible = true
      }
    },
    async chooseVideo() {
      // 使用复选框获取选中的节点
      this.selectedNodes = this.$refs.videoTree.getCheckedNodes()

      // 如果选中节点长度为0
      if (this.selectedNodes.length === 0) {
        this.$message.error("请选择至少一个视频")
      } else {
        let selectedLeafNode = null  // 初始化叶子节点为null
        let count = 0  // 计数

        // 循环查找叶子节点
        for (const node of this.selectedNodes) {
          if (!node.children || node.children.length === 0) {
            count++
            selectedLeafNode = node
          }
        }

        // 如果只有一个叶子节点
        if (count === 1) {
          this.videoPath = selectedLeafNode.videoPath
          this.videoName = selectedLeafNode.label
          this.videoDrawerVisible = false

          // 如果选择了标签组，则可以直接切换视频
          if (this.checkedLabelType != null) {
            // 将下拉列表的选项全部置空
            this.selectedPersonId = null
            this.searchRectPersonId = null

            // 加载person_id、video、record数据
            await this.loadPersonId()
            this.loadName()
            this.showVideo()
            this.showRecord()
          } else {
            this.$message.error("请选择标签组")
          }
        } else {
          this.$message.error("只能选择一个视频")
        }
      }
    },
    getLabelType() {
      if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.labelTypeDialogVisible = true
      }
    },
    closeLabelTypes() {
      this.checkedLabelType = this.finalCheckedLabelType
      this.labelTypeDialogVisible = false
    },
    async chooseLabelTypes() {
      this.finalCheckedLabelType = this.checkedLabelType
      this.labelTypeDialogVisible = false

      if (!this.videoPath && !this.videoName && !this.finalCheckedLabelType) {
        this.$message.error("请选择视频和标签组")
      } else if (this.videoPath && this.videoName && !this.finalCheckedLabelType) {
        this.$message.error("请选择标签组")
      } else if (!this.videoPath && !this.videoName && this.finalCheckedLabelType) {
        this.$message.error("请选择视频")
      } else if (this.videoPath && this.videoName && this.finalCheckedLabelType) {
        // 加载person_id、video、record数据
        await this.loadPersonId()
        this.loadName()
        this.showVideo()
        this.showRecord()
      }
    },
    async loadPersonId() {
      // 获取视频中的成员id数组
      const res = await this.$request.get("/video/getMember", {
        params: {
          videoPath: this.videoPath,
          videoName: this.videoName
        }
      })

      // 给this.personIds赋值
      this.personIds = res.data.split(',').map(item => parseInt(item.trim()))

      // 给this.colorMap赋值，后续给不同person_id的矩形框不同的颜色
      this.colorMap = {}
      const colors = ['red', 'blue']
      this.personIds.forEach((personId, index) => {
        this.rects[personId] = {}  // 初始化this.rects
        this.colorMap[personId] = colors[index]  // 初始化this.colorMap
      })
    },
    loadName() {
      // 每次调用loadName时先将this.personIdNameMap置空
      this.personIdNameMap = {}

      for (const personId of this.personIds) {
        this.$request.get("/person/getPerson", {
          params: {
            id: personId
          }
        }).then(res => {
          this.personIdNameMap[personId] = res.data[0].name
        })
      }
    },
    showVideo() {
      this.$request.get("/video/showVideo", {
        params: {
          videoPath: this.videoPath,
          videoName: this.videoName
        }
      }).then(() => {
        // 将后台http请求拼接，动态播放视频
        this.playerOptions.sources[0].src = "http://localhost:8888/video/showVideo?videoPath=" + this.videoPath + "&" + "videoName=" + this.videoName
      })
    },
    showRecord() {
      if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.$request.get("/record/getRecord", {
          params: {
            videoName: this.videoName,
            labelType: this.finalCheckedLabelType,
            uid: this.user.id,
            personId: this.selectedPersonId
          }
        }).then(res => {
          this.recordTableData = res.data
          this.createRecordSegments(this.recordTableData)
        })
      }
    },
    showRecordAndRectByUid() {
      if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.$request.get("/user/getIdByUsername", {
          params: {
            username: this.selectedUsername
          }
        }).then(res => {
          // 将当前管理员的id切换为选择的那个username的id
          this.user.id = res.data

          // 再根据切换了的id来展示record和rect
          this.showRecord()
          this.showRect()
        })
      }
    },
    createRecordSegments(recordTableData) {
      // 创建recordSegments数组用于后续显示彩带
      this.recordSegments = recordTableData.map(recordSegment => ({
        start: recordSegment.start,
        end: recordSegment.end,
        color: this.buttonColors[this.finalCheckedLabelType].find(item => item.label === recordSegment.label).color
      }))
    },
    generateProcess() {
      return this.recordSegments.map(recordSegment => {
        // 将数据库中的数据转化为秒数
        const startInSeconds = this.timeToSeconds(recordSegment.start)
        const endInSeconds = this.timeToSeconds(recordSegment.end)
        // 计算色块所占百分比
        const startPercentage = (startInSeconds / this.duration) * 100
        const endPercentage = (endInSeconds / this.duration) * 100
        const color = recordSegment.color
        return [startPercentage, endPercentage, { backgroundColor: color }]  // 要想多段显示不同的颜色，格式就是[开始，结束，{属性：属性值}]
      })
    },
    handleTimeUpdate() {
      // 获取当前时间currentTime和总时长duration，分别向下取整
      this.currentTime = Math.floor(this.$refs.videoPlayer.player.currentTime())
      this.duration = Math.floor(this.$refs.videoPlayer.player.duration())
      this.maxProgress = this.duration  // 传给进度条的最大值
      this.progress = this.currentTime  // 不断更新滑块位置

      // 随着视频播放动态显示rect
      this.showRect()
    },
    handleSliderChange() {
      this.currentTime = this.progress
      this.$refs.videoPlayer.player.currentTime(this.progress)

      // 随着视频播放动态显示rect
      this.showRect()
    },
    timeToSeconds(time) {
      // 将时分秒格式转化为秒数
      const parts = time.split(":")
      return +parts[0] * 3600 + +parts[1] * 60 + +parts[2]
    },
    formatTooltip(value) {
      // 将进度值（秒数）格式化为 "hh:mm:ss"
      const hours = Math.floor(value / 3600)
      const minutes = Math.floor((value % 3600) / 60)
      const seconds = Math.floor(value % 60)
      return `${this.formatTimeUnit(hours)}:${this.formatTimeUnit(minutes)}:${this.formatTimeUnit(seconds)}`
    },
    formatTimeUnit(value) {
      // 辅助函数，将时间单位格式化为两位数
      return value.toString().padStart(2, '0')
    },
    deleteRecord(row) {
      if (this.user.username !== this.selectedUsername && this.selectedUsername !== null) {
        this.$message.error("不可修改其他用户标注的数据")
      } else if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        // 根据表格当前行删除record，所以不需要别的参数，直接根据id删除不会有bug
        this.$request.delete("/record/deleteRecord/" + row.id).then(() => {
          // 删除完，刷新一遍record
          this.showRecord()
        })
      }
    },
    annotation(label) {
      if (this.user.username !== this.selectedUsername && this.selectedUsername !== null) {
        this.$message.error("不可修改其他用户标注的数据")
      } else if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (!this.selectedPersonId) {
        this.$message.error("请选择被标注者")
      } else {
        //获取当前视频播放时间
        const currentTime = this.formatTooltip(this.currentTime)

        // 判断当前时间是否与已标记的片段重叠
        const isOverlapping = this.recordSegments.some(recordSegment => {
          const startInSeconds = this.timeToSeconds(recordSegment.start)
          const endInSeconds = this.timeToSeconds(recordSegment.end)
          return this.currentTime > startInSeconds && this.currentTime < endInSeconds
        })

        if (isOverlapping) {  // 有重叠
          this.$message.error("标注位置有重叠")
        } else {
          if (!this.annotationInProgress) {  // 如果没有正在标注的数据
            // 显示高亮标记（竖黑线）
            this.translateXValue = (this.currentTime / this.duration) * this.$refs.slider.$el.clientWidth
            this.showMark = true

            // 将当前选择的标签设为正在标注的标签
            this.currentAnnotationLabel = label

            // 创建新的标注
            this.annotationInProgress = {
              videoName: this.videoName,
              personId: this.selectedPersonId,
              uid: this.user.id,
              start: currentTime,  // 开始时间为currentTime
              end: null,           // 结束时间暂时为空
              label: label,        // 从按钮点击中获取标签
              labelType: this.labelTypeMap[label],
            }
          } else if (this.currentAnnotationLabel === label) {  // 如果此次点击的标签与正在标注的数据标签相同
            if (this.currentTime <= this.timeToSeconds(this.annotationInProgress.start)) {
              this.$message.error("结束时间不能在开始时间之前")
            } else {
              // 如果此时即将插入数据库的新标注片段的开始时间和结束时间没有覆盖原有的片段
              const isCovering1 = this.recordSegments.some(recordSegment => {
                const startInSeconds = this.timeToSeconds(recordSegment.start)
                const endInSeconds = this.timeToSeconds(recordSegment.end)
                return startInSeconds >= this.timeToSeconds(this.annotationInProgress.start) && endInSeconds <= this.currentTime
              })

              if (isCovering1) {  // 有覆盖
                this.$message.error("新标注片段覆盖了原片段")
              } else {
                // 隐藏高亮标记（竖黑线）
                this.showMark = false

                this.annotationInProgress.end = currentTime  // 更新正在标注的数据的end
                this.saveAnnotation(this.annotationInProgress)  // 存入数据库
                this.annotationInProgress = null  // 清空正在标注的数据
                this.currentAnnotationLabel = null  // 清空正在标注的数据标签
              }
            }
          } else if (this.currentAnnotationLabel !== label) {  // 如果此次点击的标签与正在标注的数据标签不同
            if (this.currentTime <= this.timeToSeconds(this.annotationInProgress.start)) {
              this.$message.error("结束时间不能在开始时间之前")
            } else {
              // 如果此时即将插入数据库的新标注片段的开始时间和结束时间没有覆盖原有的片段
              const isCovering2 = this.recordSegments.some(recordSegment => {
                const startInSeconds = this.timeToSeconds(recordSegment.start)
                const endInSeconds = this.timeToSeconds(recordSegment.end)
                return startInSeconds >= this.timeToSeconds(this.annotationInProgress.start) && endInSeconds <= this.currentTime
              })

              if (isCovering2) {  // 有覆盖
                this.$message.error("新标注片段覆盖了原片段")
              } else {
                // 显示高亮标记（竖黑线）
                this.translateXValue = (this.currentTime / this.duration) * this.$refs.slider.$el.clientWidth
                this.showMark = true

                this.annotationInProgress.end = currentTime  // 更新正在标注的数据的end 
                this.saveAnnotation(this.annotationInProgress)  // 存入数据库
                this.currentAnnotationLabel = label // 将当前选择的标签设为正在标注的标签

                // 创建新的标注
                this.annotationInProgress = {
                  videoName: this.videoName,
                  personId: this.selectedPersonId,
                  uid: this.user.id,
                  start: currentTime,  // 开始时间为currentTime
                  end: null,           // 结束时间暂时为空
                  label: label,        // 从按钮点击中获取标签
                  labelType: this.labelTypeMap[label],
                }
              }
            }
          }
        }
      }   
    },
    saveAnnotation(annotation) {
      // 将标注数据保存到数据库
      this.$request.post("/record/createRecord", annotation).then(() => {
        // 刷新record
        this.showRecord()
      })
    },
    cancelAnnotation() {
      if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.showMark = false
        this.annotationInProgress = null
      }
    },
    updateRecord(index, row) {
      if (this.user.username !== this.selectedUsername && this.selectedUsername !== null) {
        this.$message.error("不可修改其他用户标注的数据")
      } else if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else {
        if (this.isEditingRow === null) {  // 如果是第一次点击编辑，开启编辑模式
          // 禁止操作下拉框
          this.isSearchDisabled = true
          this.isSelectedPersonIdDisabled = true

          // 赋初值
          this.isEditingRow = index
          this.firstStart = row.start
          this.firstEnd = row.end
          this.firstLabel = row.label
        } else if (this.isEditingRow === index) {  // 如果是第二次点击编辑，即可把修改的数据保存
          // 允许操作下拉框
          this.isSearchDisabled = false
          this.isSelectedPersonIdDisabled = false

          // 把正在编辑设为null
          this.isEditingRow = null

          // 先检查修改的数据合不合法
          const isGood = this.isGood(row)

          if (!isGood) {
            this.$message.error("请检查修改数据合法性")
            row.start = this.firstStart
            row.end = this.firstEnd
            row.label = this.firstLabel
          } else {
            this.$request.post("/record/updateRecord", {
              id: row.id,
              videoName: row.videoName,
              personId: row.personId,
              uid: row.uid,
              start: row.start,
              end: row.end,
              label: row.label,
              labelType: row.labelType,
            }).then(() => {
              // 刷新record
              this.showRecord()
            })
          }
        } else {
          this.$message.error("请先修改完当前行")
        }
      }
    },
    isGood(row) {
      // 备份原始 recordSegments
      let originalSegments = [...this.recordSegments]

      // 找到当前行对应originalSegments里的下标
      const rowIndex = originalSegments.findIndex(segment => {
        return segment.start === this.firstStart && segment.end === this.firstEnd;
      })

      // 将当前行对应的片段去掉
      if (rowIndex !== -1) {
        originalSegments = originalSegments.filter((segment, index) => index !== rowIndex);
      }

      // 如果开始时间大于等于结束时间，返回false
      if (row.start >= row.end) {
        return false
      }

      // 转化为秒
      const startUpdateInSeconds = this.timeToSeconds(row.start)
      const endUpdateInSeconds = this.timeToSeconds(row.end)

      // 检查时间范围是否与现有片段重叠
      const isOverlap = originalSegments.some(segment => {
        const startInSeconds = this.timeToSeconds(segment.start)
        const endInSeconds = this.timeToSeconds(segment.end)
        return (
          (startUpdateInSeconds > startInSeconds && startUpdateInSeconds < endInSeconds) ||
          (endUpdateInSeconds > startInSeconds && endUpdateInSeconds < endInSeconds) ||
          (startUpdateInSeconds <= startInSeconds && endUpdateInSeconds >= endInSeconds)
        )
      })

      // 如果重叠，返回false
      if (isOverlap) {
        return false
      }

      // 如果上述都通过则返回true
      return true
    },
    getLabelColor(label) {
      // 根据label设置this.buttonColors
      return this.buttonColors[this.finalCheckedLabelType].find(item => item.label === label).color
    },
    handlePause() {
      this.isPaused = true
    },
    handlePlay() {
      this.isPaused = false
    },

    
    startDraw() {
      if (this.user.username !== this.selectedUsername && this.selectedUsername !== null) {
        this.$message.error("不可修改其他用户标注的数据")
      } else if (!this.videoPath && !this.videoName && !this.finalCheckedLabelType) {
        this.$message.error("请选择视频和标签组")
      } else if (this.videoPath && this.videoName && !this.finalCheckedLabelType) {
        this.$message.error("请选择标签组")
      } else if (!this.videoPath && !this.videoName && this.finalCheckedLabelType) {
        this.$message.error("请选择视频")
      } else if (this.videoPath && this.videoName && this.finalCheckedLabelType) {
        if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
        } else if (!this.selectedPersonId) {
          this.$message.error("请选择被标注者")
        } else if (!this.searchRectPersonId) {
          this.$message.error("请选择对应被标注者的矩形框")
        } else if (this.selectedPersonId !== this.searchRectPersonId && this.searchRectPersonId !== '全选') {
          this.$message.error("请选择相同的被标注者")
        } else {
          if (this.isPaused === false) {
            this.$message.error("请先暂停视频才能开始画框")
          } else {
            // 切换startDrawMode
            this.startDrawMode = !this.startDrawMode

            if (this.startDrawMode === true) {  // 开始画框
              // 禁用下拉框操作
              this.isSearchDisabled = true
              // 如果是非全选情况下就禁用，否则全选情况下可以切换person_id来进行不同矩形框绘制
              if (this.searchRectPersonId !== '全选') { this.isSelectedPersonIdDisabled = true }

              // 获取画布canvas的父容器div
              const canvasContainer1 = document.querySelector('.canvas-container')
              // 将画布置为不可穿透，也就是绘画时不可点击暂停和播放
              canvasContainer1.style.pointerEvents = 'auto'  

              // 监听函数
              this.canvas.on('mouse:down', this.mouseDown)
              this.canvas.on('mouse:up', this.mouseUp)
              this.canvas.on('object:moving', this.objectMoving)
              this.canvas.on('object:scaling', this.objectScaling)
              this.canvas.on('object:rotating', this.objectRotating)
              this.canvas.on('selection:created', this.selectionCreated)
              this.canvas.on('selection:updated', this.selectionUpdated)
              document.addEventListener('keydown', this.deleteRect)
            } else {  // 结束画框
              // 允许下拉框操作
              this.isSearchDisabled = false
              this.isSelectedPersonIdDisabled = false

              // 获取画布canvas的父容器div
              const canvasContainer2 = document.querySelector('.canvas-container')
              // 将画布置为可穿透，也就是绘画时可点击暂停和播放
              canvasContainer2.style.pointerEvents = 'none'

              // 每次结束画框后取消所有框的选中状态，否则会出bug
              this.canvas.discardActiveObject()
              this.canvas.renderAll()

              // 创建或更新face
              for (const personId in this.rects) {
                for (const time in this.rects[personId]) {
                  if (this.formatTooltip(this.currentTime) === time) {  // 将当前time的所有框传给后台
                    const rect = this.rects[personId][time]
                    this.$request.post("/face/saveOrUpdateFace", {
                      videoName: this.videoName,
                      personId: rect.personId,
                      uid: rect.uid,
                      time: this.formatTooltip(this.currentTime),
                      x0: (rect.aCoords.tl.x / this.canvas.width) * 100,
                      y0: (rect.aCoords.tl.y / this.canvas.height) * 100,
                      x1: (rect.aCoords.br.x / this.canvas.width) * 100,
                      y1: (rect.aCoords.br.y / this.canvas.height) * 100,
                    }).then(() => {
                      this.showRect()
                    })
                  }
                }
              }
            }
          }
        }
      }
    },
    async getRect() {
      if (this.searchRectPersonId === '全选') {  // 显示全部person_id对应的矩形框
        // 设置参数params
        const params = {
          videoName: this.videoName,
          uid: this.user.id,
        }

        // 从数据库获取face表的数据，并按照time由小到大排列，赋值给this.faces
        // 得保证在showRect中先执行完getRect再执行下述代码，所以用异步
        const res = await this.$request.get("/face/getFace", { params })

        // 排序
        res.data.sort((a, b) => {
          return a.time.localeCompare(b.time)
        })
        this.faces = res.data
      } else {  // 显示person_id对应的矩形框
        // 设置参数params
        const params = {
          videoName: this.videoName,
          uid: this.user.id,
          personId: this.searchRectPersonId
        }

        // 从数据库获取face表的数据，并按照time由小到大排列，赋值给this.faces
        // 得保证在showRect中先执行完getRect再执行下述代码，所以用异步
        const res = await this.$request.get("/face/getFaceByPersonId", { params })

        // 排序
        res.data.sort((a, b) => {
          return a.time.localeCompare(b.time)
        })
        this.faces = res.data
      }
    },
    async showRect() {
      // 先更新一下this.faces
      await this.getRect()

      // 清除画布上的所有对象
      this.canvas.getObjects().forEach(obj => {
        this.canvas.remove(obj)
      })

      // 获取当前时间
      const currentTime = this.formatTooltip(this.currentTime)

      // 遍历所有的person_id，这里拿this.personIds获取所有person_id
      for (const personId of this.personIds) {
        // 拿到距离最近的时间点的框的信息closestFace
        let closestFace = null
        for (const face of this.faces) {
          if (face.personId === personId && face.time <= currentTime) {
            // 如果最近的矩形信息不存在或者当前矩形的时间更接近当前时间，则更新最近的矩形信息
            if (!closestFace || face.time >= closestFace.time) {
              closestFace = face
            }
          }
        }

        // 创建矩形框
        if (closestFace) {
          const rect = new fabric.Rect({
            left: closestFace.x0 * this.canvas.width / 100,
            top: closestFace.y0 * this.canvas.height / 100,
            width: (closestFace.x1 - closestFace.x0) * this.canvas.width / 100,
            height: (closestFace.y1 - closestFace.y0) * this.canvas.height / 100,
            fill: 'transparent',
            stroke: this.colorMap[closestFace.personId],
            strokeWidth: 1,
            strokeUniform: true,
            personId: closestFace.personId,
            uid: closestFace.uid,
            time: closestFace.time
          })
          this.canvas.add(rect)
        }
      }
      this.canvas.renderAll()
    },
    mouseDown(event) {
      if (!this.isDrawing) {
        this.isDrawing = true
        this.startPoint = this.canvas.getPointer(event.e)
      }
    },
    mouseUp(event) {
      if (this.isDrawing) {
        this.isDrawing = false
        this.endPoint = this.canvas.getPointer(event.e)

        // 计算四个属性值
        let left = Math.min(this.startPoint.x, this.endPoint.x)
        let top = Math.min(this.startPoint.y, this.endPoint.y)
        let width = Math.abs(this.startPoint.x - this.endPoint.x)
        let height = Math.abs(this.startPoint.y - this.endPoint.y)

        // 确保矩形框不越界
        if (left < 0) {
          left = 0
        }
        if (top < 0) {
          top = 0
        }
        if (left + width > this.canvas.width) {
          width = this.canvas.width - left
        }
        if (top + height > this.canvas.height) {
          height = this.canvas.height - top
        }

        // 获取当前时间
        const currentTime = this.formatTooltip(this.currentTime)

        // 如果this.rects当前person_id的当前时间里没有数据，则赋值
        if (!this.rects[this.selectedPersonId][currentTime]) {
          // 清除画布上的所有矩形框
          this.canvas.getObjects().forEach(obj => {
            if (obj.personId === this.selectedPersonId) {
              this.canvas.remove(obj)
            }
          })

          // 创建矩形框对象
          this.rects[this.selectedPersonId][currentTime] = new fabric.Rect({
            left: left,
            top: top,
            width: width,
            height: height,
            fill: 'transparent',
            stroke: this.colorMap[this.selectedPersonId],
            strokeWidth: 1,
            strokeUniform: true,
            personId: this.selectedPersonId,
            uid: this.user.id,
          })

          // 加入画布
          this.canvas.add(this.rects[this.selectedPersonId][currentTime])

          // 画完之后将不是当前person_id以及当前时间对应的this.rects的rect对象remove
          for (const time in this.rects[this.selectedPersonId]) {
            if (time !== currentTime) {
              const rect = this.rects[this.selectedPersonId][time]
              if (rect) {
                this.canvas.remove(rect)
              }
            }
          }
          this.canvas.renderAll()  // 重新渲染
        }
      }
    },
    objectMoving() {
      this.isDrawing = false
    },
    objectScaling() {
      this.isDrawing = false
    },
    objectRotating() {
      this.isDrawing = false
    },
    selectionCreated(event) {  // 第一次选择时触发
      // 以免出现bug，重新赋值一遍
      this.selectedPersonId = event.selected[0].personId

      // 将当前选择的对象，给予他新的时间属性
      this.rects[event.selected[0].personId][this.formatTooltip(this.currentTime)] = event.selected[0]
    },
    selectionUpdated(event) {  // 选择不同的对象时触发
      // 以免出现bug，重新赋值一遍
      this.selectedPersonId = event.selected[0].personId
      
      // 将当前选择的对象，给予他新的时间属性
      this.rects[event.selected[0].personId][this.formatTooltip(this.currentTime)] = event.selected[0]
    },
    deleteRect(event) {
      if (event.key === 'd' && this.canvas.getActiveObject()) {
        // 拿到当前选择的矩形框对象
        const selectedObject = this.canvas.getActiveObject()

        // 判断保证只能删除对应的person_id的矩形框
        if (selectedObject.personId !== this.selectedPersonId) {
          this.$message.error('不能删除不对应被标注者的矩形框')
        } else {
          // 从画布中先移除选中矩形框
          this.canvas.remove(selectedObject)
          this.canvas.renderAll()

          // 删除this.rects和this.faces
          delete this.rects[selectedObject.personId][this.formatTooltip(this.currentTime)]
          this.faces = this.faces.filter(face => {
            return !(face.personId === selectedObject.personId && face.time === this.formatTooltip(this.currentTime))
          })

          // 删除后台数据，如果在数据库没有对应的time会执行这个请求但是删不掉，无所谓
          this.$request.delete("/face/deleteFace", {
            params: {
              videoName: this.videoName,
              personId: selectedObject.personId,
              uid: selectedObject.uid,
              time: this.formatTooltip(this.currentTime)
            }
          })
        }
      }       
    },
    jumpToTime(time) {
      this.$refs.videoPlayer.player.currentTime(this.timeToSeconds(time))
    },
    addVideoOperationListener() {
      // 等视频可播放时添加按键监听
      document.addEventListener('keydown', this.videoOperation)
    },
    videoOperation(event) {
      try {
        switch (event.key) {
          case ' ':  // 暂停或播放
            if (this.isPaused === false) {
              this.$refs.videoPlayer.player.pause()
            } else {
              this.$refs.videoPlayer.player.play()
            }
            break
          case 'z':  // 快退5秒
            this.$refs.videoPlayer.player.currentTime(this.currentTime - 5)
            break
          case 'x':  // 快退1秒
            this.$refs.videoPlayer.player.currentTime(this.currentTime - 1)
            break
          case 'c':  // 快进1秒
            this.$refs.videoPlayer.player.currentTime(this.currentTime + 1)
            break
          case 'v':  // 快进5秒
            this.$refs.videoPlayer.player.currentTime(this.currentTime + 5)
            break
        }
      } catch (error) {
        console.log(error)
      }
    },
    exportRecord() {  // 导出数据
      if (!this.videoPath && !this.videoName && !this.finalCheckedLabelType) {
        this.$message.error("请选择视频和标签组")
      } else if (this.videoPath && this.videoName && !this.finalCheckedLabelType) {
        this.$message.error("请选择标签组")
      } else if (!this.videoPath && !this.videoName && this.finalCheckedLabelType) {
        this.$message.error("请选择视频")
      } else if (this.videoPath && this.videoName && this.finalCheckedLabelType) {
        if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
        } else if (!this.selectedPersonId) {
          this.$message.error("请选择被标注者")
        } else {
          // 根据这些来导出
          const params = {
            videoName: this.videoName,
            labelType: this.finalCheckedLabelType,
            personId: this.selectedPersonId,
            uid: this.user.id,
            name: this.personIdNameMap[this.selectedPersonId]
          }

          // 构建导出的URL
          const exportURL = "http://localhost:8888/record/exportRecord?" + Object.entries(params).map(([key, value]) => key + '=' + value).join('&')

          // 打开exportURL对应的导出文件的弹窗
          window.open(exportURL)
        }
      }
    }
  }
}
</script>
    
<style scoped>
  /* 添加下划线和更改颜色 */
  .el-button.is-text1 {
    text-decoration: underline;
    color: #409EFF;
  }
  .el-button.is-text2 {
    text-decoration: underline;
    color: #909399;
  }
  .canvas-container {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 2;
    pointer-events: none;
  }
</style>