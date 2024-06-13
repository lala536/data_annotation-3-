<template>
  <div>
    <el-container style="height: 89vh">
      <!-- 标注部分 -->
      <el-main style="box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); width: 66%">
        <!-- 顶部 -->
        <div style="margin-bottom: 10px;">
          <!-- 选择视频按钮 -->
          <el-button type="primary" plain @click="getVideo">视频</el-button>
          <!-- 选择标签组按钮 -->
          <el-button type="primary" plain @click="getLabelType">标签</el-button>
          <!-- 画框 -->
          <el-button type="primary" plain @click="startDraw">{{ startDrawMode ? '结束' : '画框' }}</el-button>

          <!-- 选择播放器 -->
          <el-link style="margin-left: 10px;" :underline="false">播放器：</el-link>
          <el-select ref="playerSelect" v-model="selectedVideoIndex" @change="chooseVideoPlayer(selectedVideoIndex)" style="width: 80px;">
            <el-option
              :label="'全选'"
              :value="0">
            </el-option>
            <el-option
              v-for="video in selectedVideos"
              :key="video.index"
              :label="video.index"
              :value="video.index">
            </el-option>
          </el-select>

          <!-- 选择被标注者 -->
          <el-link style="margin-left: 10px;" :underline="false">被标注者：</el-link>
          <el-select v-model="selectedPersonId" @change="showRecord" clearable style="width: 80px;">
            <el-option v-if="showSelectAll"
              :label="'全选'"
              :value="'全选'">
            </el-option>
            <el-option
              v-for="(name, personId) in personIdNameMap[selectedVideoIndex]"
              :key="parseInt(personId)"
              :label="name"
              :value="parseInt(personId)">
            </el-option>
          </el-select>

          <el-link style="margin-left: 10px;" :underline="false">矩形框：</el-link>
          <!-- 根据person_id查询Rect -->
          <el-select v-model="selectedRect" @change="showRect" clearable style="width: 80px;">
            <el-option
              :label="'全选'"
              :value="'全选'">
            </el-option>
            <el-option
              v-for="(name, personId) in personIdNameMap[selectedVideoIndex]"
              :key="parseInt(personId)"
              :label="name"
              :value="parseInt(personId)">
            </el-option>
          </el-select>
      
          <!-- 选择视频 -->
          <el-drawer title="视频列表" direction="ltr" :visible.sync="videoDrawer" size="50%">
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

          <!-- 选择标签 -->
          <el-dialog title="标签" :visible.sync="labelTypeDialog" width="30%" @close="closeLabelType">
            <el-radio-group v-show="showSingleLabelType" v-model="selectedLabelType">
              <el-radio v-for="singleLabelType in singleLabelTypes" :label="singleLabelType" :key="singleLabelType">{{ singleLabelType }}</el-radio>
            </el-radio-group>
            <el-radio-group v-show="showMultiLabelType" v-model="selectedLabelType">
              <el-radio v-for="multiLabelType in multiLabelTypes" :label="multiLabelType" :key="multiLabelType">{{ multiLabelType }}</el-radio>
            </el-radio-group>
            <div slot="footer" class="dialog-footer">
              <el-button @click="closeLabelType">取 消</el-button>
              <el-button type="primary" @click="chooseLabelType">选 择</el-button>
            </div>
          </el-dialog>
        </div>

        <!-- 播放器，最多四个 -->
        <div class="video-players-container">
          <div v-show="showPlayer1" class="video-player1">
            <video-player 
              class="vjs-custom-skin"
              ref="videoPlayer1"
              :playsinline="true"
              :options="playerOptions[0]"
              @timeupdate="timeUpdate(1)"
              @ended="playNextVideo(1)"
              @loadeddata="set(1)"
            ></video-player>
            <div class="canvas-container1">
              <canvas id="canvas1"></canvas>
            </div>
          </div>

          <div v-show="showPlayer2" class="video-player2">
            <video-player 
              class="vjs-custom-skin"
              ref="videoPlayer2"
              :playsinline="true"
              :options="playerOptions[1]"
              @timeupdate="timeUpdate(2)"
              @ended="playNextVideo(2)"
              @loadeddata="set(2)"
            ></video-player>
            <div class="canvas-container2">
              <canvas id="canvas2"></canvas>
            </div>
          </div>

          <div v-show="showPlayer3" class="video-player3">
            <video-player 
              class="vjs-custom-skin"
              ref="videoPlayer3"
              :playsinline="true"
              :options="playerOptions[2]"
              @timeupdate="timeUpdate(3)"
              @ended="playNextVideo(3)"
              @loadeddata="set(3)"
            ></video-player>
            <div class="canvas-container3">
              <canvas id="canvas3"></canvas>
            </div>
          </div>

          <div v-show="showPlayer4" class="video-player4">
            <video-player 
              class="vjs-custom-skin"
              ref="videoPlayer4"
              :playsinline="true"
              :options="playerOptions[3]"
              @timeupdate="timeUpdate(4)"
              @ended="playNextVideo(4)"
              @loadeddata="set(4)"
            ></video-player>
            <div class="canvas-container4">
              <canvas id="canvas4"></canvas>
            </div>
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
            @change="sliderChange">
          </vue-slider>
          <div v-show="showMark" :style="'width: 1px; height: 26px; background-color: black; transform: translateX(' + translateXValue + 'px) translateY(-7.5px); position: relative; z-index: 1 !important;'"></div>
        </div>

        <!-- 打标签按钮 -->
        <div style="position: absolute; bottom: 80px; left: 48px;">
          <el-button v-for="item in this.buttonColors[this.finalSelectedLabelType]" :key="item" @click="annotation(item.label)" :style="{ backgroundColor: item.color }">{{ item.label }}</el-button>
          <el-button v-show="this.finalSelectedLabelType" @click="cancelAnnotation()">取消</el-button>
        </div>
      </el-main>

      <!-- 展示部分 -->
      <el-main style="box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); width: 34%">
        <!-- 表格 -->
        <el-table :data="recordData" height="618" :default-sort = "{prop: 'start', order: 'ascending'}" border :highlight-current-row="true" style="width: 100%;">
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
                <el-option v-for="label in buttonColors[finalSelectedLabelType].map((item) => item.label)" :key="label" :label="label" :value="label"></el-option>
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
      //////////////////////////登录、用户信息//////////////////////////
      user: JSON.parse(localStorage.getItem('user') || '{}'),  // 获取当前登录的user信息
      //////////////////////////选择视频并播放//////////////////////////
      videoDrawer: false,
      treeData: [],  // 这里存放构建好的树形数据
      treeProps: {
        label: 'label',  // 每个节点显示的文本
        children: 'children'  // 子节点数组的字段名
      },
      selectedVideos: null,
      videoPaths: [],
      videoNames: [],
      width: 0,
      height: 0,
      //////////////////////////选择标签组//////////////////////////
      labelTypeDialog: false,
      selectedLabelType: null,
      finalSelectedLabelType: null,
      singleLabelTypes: [],
      multiLabelTypes: [],
      labelTypeMap: [],
      showSingleLabelType: false,
      showMultiLabelType: true,
      buttonColors: [],
      //////////////////////////播放器下拉框//////////////////////////
      selectedVideoIndex: 0,
      showPlayer1: true,
      showPlayer2: true,
      showPlayer3: true,
      showPlayer4: true,
      lastIndex: 0,
      //////////////////////////播放器和进度条设置//////////////////////////
      durations: [],
      currentTime: null,
      progress: 0,
      maxProgress: 0,
      showMark: false,
      translateXValue: 0,
      recordSegments: [],  // 存储Record数组
      isPlaying: false,  // 是否播放
      order: [],
      nextVideo: {},
      playerOptions: [
        {  // 视频播放器选项
          playbackRates: [0.5, 1.0, 1.5, 2.0],  // 可选的播放速度
          autoplay: false,  // 自动播放
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
        {  // 视频播放器选项
          playbackRates: [0.5, 1.0, 1.5, 2.0],  // 可选的播放速度
          autoplay: false,  // 自动播放
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
        {  // 视频播放器选项
          playbackRates: [0.5, 1.0, 1.5, 2.0],  // 可选的播放速度
          autoplay: false,  // 自动播放
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
        {  // 视频播放器选项
          playbackRates: [0.5, 1.0, 1.5, 2.0],  // 可选的播放速度
          autoplay: false,  // 自动播放
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
        }
      ],
      //////////////////////////被标注者下拉框//////////////////////////
      personIds: {},  // 该视频的所有person_id
      personIdNameMap: {}, // person_id和名字的映射
      colorMap: {},
      selectedPersonId: null,
      showSelectAll: true,
      //////////////////////////打标签和数据表格展示//////////////////////////
      recordData: [],
      isEditingRow: null,
      annotationInProgress: null,  // 用于存储当前正在标注的数据
      currentAnnotationLabel: null,  // 用于存储当前标注的标签
      firstStart: null,  // 初始的开始时间
      firstEnd: null,  // 初始的结束时间
      firstLabel: null,  // 初始的标签
      //////////////////////////矩形框下拉框//////////////////////////
      selectedRect: null,  // 用于查询Rect
      //////////////////////////画布和画框//////////////////////////
      canvas: {},
      startDrawMode: false,  // 开启画框模式
      isDrawing: false,  // 正在画框
      startPoint: null,  // 鼠标起始点
      endPoint: null,  // 鼠标结束点
      rects: {},  // 保存每个播放器以及person_id对应的矩形框，用于发送数据到数据库，三维对象
      faces: {},  // 用于从数据库中获取每一条face数据
    }
  },
  async created() {
    // 加载视频和标签组
    await this.loadVideo()
    await this.loadLabelType()
  },
  mounted() {
    this.width = this.$refs.videoPlayer1.$el.offsetWidth
    this.height = this.$refs.videoPlayer1.$el.offsetHeight
  },
  methods: {
    ///////////////// 选择视频并播放 /////////////////
    // 加载视频
    async loadVideo() {
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
              parent: [courseNode]  // 父节点
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
                    parent: [courseNode, groupNode],  // 父节点
                  }
                }

                // 在前缀节点下添加视频节点
                videoPrefixes[videoPrefix].children.push({
                  id: video.id,
                  label: video.videoName,
                  videoPath: video.videoPath,  // 添加一条视频路径信息以供后续代码获取
                  parent: [courseNode, groupNode, videoPrefixes[videoPrefix]]  // 保留父节点
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

    // 打开视频drawer
    getVideo() {
      if (this.selectedVideoIndex !== 0) {
        this.$message.error("请切换到全选再选择视频")
      } else if (this.selectedPersonId !== null) {
        this.$message.error("请清空被标注者")
      } else if (this.selectedRect !== null) {
        this.$message.error("请清空矩形框")
      } else if (this.annotationInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.videoDrawer = true
      }
    },

    // 选择视频
    async chooseVideo() {
      // 使用复选框获取选中的节点
      const selectedNodes = this.$refs.videoTree.getCheckedNodes()

      // 过滤出叶子节点并map出自己想要的信息
      this.selectedVideos = selectedNodes
      .filter(node => !node.children || node.children.length === 0)
      .map((node, index) => ({
        index: index + 1,
        videoPath: node.videoPath, 
        videoName: node.label, 
        parent: node.parent,
        order: node.parent[2].children.findIndex(child => child.label === node.label),
      }))

      // 检查所选视频是否来自同一小组
      const sameGroup = this.selectedVideos.every(video => video.parent[1].label === this.selectedVideos[0].parent[1].label)
      // 每个摄像头下只能选择最多一个视频
      const uniqueCamera = new Set(this.selectedVideos.map(video => video.parent[2].label))
      const isUniqueCamera = [...uniqueCamera].every(camera => this.selectedVideos.filter(video => video.parent[2].label === camera).length <= 1)

      // 如果选中节点长度为0
      if (this.selectedVideos.length === 0) {
        this.$message.error("还未选择视频")
        return
      }

      if (!sameGroup) {
        this.$message.error("请选择同一小组的视频")
        return
      }

      if (!isUniqueCamera) {
        this.$message.error("每个摄像头下只能选择最多一个视频")
        return
      }

      this.videoDrawer = false

      // 清空当前选择
      this.videoPaths = []
      this.videoNames = []

      // 为this.order赋初值
      for (let i = 0; i < this.selectedVideos.length; i++) {
        this.order[i] = this.selectedVideos[i].order
      }

      // 将视频信息存入数组
      for(const node of this.selectedVideos) {
        this.videoPaths.push(node.videoPath)
        this.videoNames.push(node.videoName)
      }

      // 在播放视频前将当前用不到的播放器的src置空
      for (let i = this.selectedVideos.length; i < this.playerOptions.length; i++) {
        this.playerOptions[i].sources[0].src = ''
      }

      // 播放视频、展示标签
      await this.showVideo()
      this.showRecord()
    },

    // 播放视频
    async showVideo() {
      for (let i = 0; i < this.selectedVideos.length; i++) {
        this.$request.get("/video/showVideo", {
          params: {
            videoPath: this.videoPaths[i],
            videoName: this.videoNames[i]
          }
        })
      }
      for (let i = 0; i < this.selectedVideos.length; i++) {
        this.playerOptions[i].sources[0].src = "http://localhost:8888/video/showVideo?videoPath=" + this.videoPaths[i] + "&" + "videoName=" + this.videoNames[i];
      }
    },

    ///////////////// 选择标签组 /////////////////
    // 加载标签
    async loadLabelType() {
      // 获取所有label数据
      const res = await this.$request.get("/label/getAllLabel")

      this.singleLabelTypes = [...new Set(res.data.filter(label => label.isGroup === 0).map(label => label.labelType))]
      this.multiLabelTypes = [...new Set(res.data.filter(label => label.isGroup === 1).map(label => label.labelType))]

      this.buttonColors = []
      res.data.forEach((item) => {
        const { labelType, label, color } = item
        if (!this.buttonColors[labelType]) {
          this.buttonColors[labelType] = []
        }
        this.buttonColors[labelType].push({ label, color })

        // 创建label到labelType的映射
        this.labelTypeMap[label] = labelType
      })
    },

    // 打开标签dialog
    getLabelType() {
      if (this.annotationInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.labelTypeDialog = true
      }
    },

    // 关闭标签dialog
    closeLabelType() {
      this.selectedLabelType = this.finalSelectedLabelType
      this.labelTypeDialog = false
    },

    // 选择标签组
    chooseLabelType() {
      this.finalSelectedLabelType = this.selectedLabelType
      this.labelTypeDialog = false
      this.showRecord()
    },

    ///////////////// 播放器和进度条设置 /////////////////
    // 选择播放器
    async chooseVideoPlayer(videoIndex) {
      if (this.annotationInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        if (videoIndex !== 0) {  // 单个视频
          // 展示单个视频标注标签
          this.showMultiLabelType = false
          this.showSingleLabelType = true

          // 暂停所有播放器
          this.$refs.videoPlayer1.player.pause()
          this.$refs.videoPlayer2.player.pause()
          this.$refs.videoPlayer3.player.pause()
          this.$refs.videoPlayer4.player.pause()

          // 调整大小，将当前选择的播放器放大2倍，其余的缩小成0%
          document.querySelector('.video-player1').style.width = videoIndex === 1 ? '200%' : '0%'
          document.querySelector('.video-player2').style.width = videoIndex === 2 ? '200%' : '0%'
          document.querySelector('.video-player3').style.width = videoIndex === 3 ? '200%' : '0%'
          document.querySelector('.video-player4').style.width = videoIndex === 4 ? '200%' : '0%'

          // 将其他播放器隐藏
          this.showPlayer1 = videoIndex === 1 ? true : false
          this.showPlayer2 = videoIndex === 2 ? true : false
          this.showPlayer3 = videoIndex === 3 ? true : false 
          this.showPlayer4 = videoIndex === 4 ? true : false

          if (this.lastIndex === 0) {
            // 如果上一次是小组视频，此次将放大
            this.canvas[videoIndex].setDimensions({
              width: this.width * 2,
              height: this.height * 2
            })
          } else {
            // 如果上一次选的是单个视频，此次要先将上次选的视频画布缩小
            this.canvas[this.lastIndex].setDimensions({
              width: this.width,
              height: this.height
            })
            // 再放大当前画布
            this.canvas[videoIndex].setDimensions({
              width: this.width * 2,
              height: this.height * 2
            })
          }
          
          // 设置画布穿透
          document.querySelector('.canvas-container1').style.pointerEvents = videoIndex === 1 ? 'none' : 'auto'
          document.querySelector('.canvas-container2').style.pointerEvents = videoIndex === 2 ? 'none' : 'auto'
          document.querySelector('.canvas-container3').style.pointerEvents = videoIndex === 3 ? 'none' : 'auto'
          document.querySelector('.canvas-container4').style.pointerEvents = videoIndex === 4 ? 'none' : 'auto'

          // 加载学生id和姓名
          await this.loadPersonId(videoIndex)
          this.loadName(videoIndex)

          // 记录上一次最后选择的播放器索引
          this.lastIndex = videoIndex
          this.selectedPersonId = null
          this.selectedRect = null
          this.showSelectAll = false

          // 清除画布上的所有对象
          this.canvas[videoIndex].getObjects().forEach(obj => {
            this.canvas[videoIndex].remove(obj)
          })
        } else {  // 多个视频
          // 展示多个视频标注标签
          this.showSingleLabelType = false
          this.showMultiLabelType = true

          // 暂停所有播放器
          this.$refs.videoPlayer1.player.pause()
          this.$refs.videoPlayer2.player.pause()
          this.$refs.videoPlayer3.player.pause()
          this.$refs.videoPlayer4.player.pause()

          // 重置所有缩放
          document.querySelector('.video-player1').style.width = '100%'
          document.querySelector('.video-player2').style.width = '100%'
          document.querySelector('.video-player3').style.width = '100%'
          document.querySelector('.video-player4').style.width = '100%'

          // 将所有播放器设为可见
          this.showPlayer1 = true
          this.showPlayer2 = true
          this.showPlayer3 = true
          this.showPlayer4 = true

          // 如果选了全选，就把上一次选择的画布缩小
          this.canvas[this.lastIndex].setDimensions({
            width: this.width,
            height: this.height
          })

          // 重置所有画布穿透
          document.querySelector('.canvas-container1').style.pointerEvents = 'none'
          document.querySelector('.canvas-container2').style.pointerEvents = 'none'
          document.querySelector('.canvas-container3').style.pointerEvents = 'none'
          document.querySelector('.canvas-container4').style.pointerEvents = 'none'

          this.personIdNameMap = {}
          this.finalSelectedLabelType = null
          this.selectedPersonId = null
          this.selectedRect = null
          this.showSelectAll = true

          // 清除画布上的所有对象
          for (let i = 1; i <= this.selectedVideos.length; i++) {
            this.canvas[i].getObjects().forEach(obj => {
              this.canvas[i].remove(obj)
            })
          }
        }
      }
    },

    // 加载person_id数据
    async loadPersonId(videoIndex) {
      // 获取视频中的成员id数组
      const res = await this.$request.get("/video/getMember", {
        params: {
          videoPath: this.videoPaths[videoIndex-1],
          videoName: this.videoNames[videoIndex-1]
        }
      })

      // 给this.personIds赋值
      this.personIds[videoIndex] = res.data.split(',').map(item => parseInt(item.trim()))
      console.log("--------this.personIds--------")
      console.log(this.personIds)
      console.log("--------this.personIds--------")

      // 给this.colorMap赋值，后续给不同person_id的矩形框不同的颜色
      this.colorMap[videoIndex] = {}
      const colors = ['yellow', 'blue']
      this.personIds[videoIndex].forEach((personId, index) => {
        this.colorMap[videoIndex][personId] = colors[index]  // 初始化this.colorMap
      })
      console.log("--------this.colorMap--------")
      console.log(this.colorMap)
      console.log("--------this.colorMap--------")
    },

    // 加载姓名
    loadName(videoIndex) {
      // 每次调用loadName时先将this.personIdNameMap置空
      this.personIdNameMap[videoIndex] = {}

      for (const personId of this.personIds[videoIndex]) {
        this.$request.get("/person/getPerson", {
          params: {
            id: personId
          }
        }).then(res => {
          // person_id与name的映射
          this.personIdNameMap[videoIndex][personId] = res.data[0].name
        })
      }
      console.log("--------this.personIdNameMap--------")
      console.log(this.personIdNameMap)
      console.log("--------this.personIdNameMap--------")
    },

    // 初始化
    async set(videoIndex){
      if (this.playerOptions[videoIndex-1].sources[0].src !== '') {
        // 获取每个播放器的进度存入this.durations
        this.durations[videoIndex-1] = this.$refs[`videoPlayer${videoIndex}`].player.duration()

        // 如果全选则在这里找一个最大的进度作为进度条进度
        if (this.selectedVideoIndex === 0) {
          this.maxProgress = Math.floor(Math.max(...this.durations))
        }

        // 初始化画布
        this.canvas[videoIndex] = new fabric.Canvas(`canvas${videoIndex}`, {
          backgroundColor: 'rgba(115, 0, 0, 0.2)',
          width: this.width,
          height: this.height
        })

        // 加载学生id和姓名，在这里加载主要是为了能够在全选的时候展示所有的框
        await this.loadPersonId(videoIndex)
        this.loadName(videoIndex)

        // 为矩形框this.rects三维对象赋值
        this.rects[videoIndex] = {}
        this.personIds[videoIndex].forEach(personId => {
          this.rects[videoIndex][personId] = {}  // 初始化this.colorMap
        })
        console.log("--------初始化this.rects--------")
        console.log(this.rects)
        console.log("--------初始化this.rects--------")

        this.nextVideo[videoIndex] = this.selectedVideos[videoIndex-1].parent[2].children[this.order[videoIndex-1]]
        
        // 监听键盘
        document.addEventListener('keydown', this.keyDown)
        
      }
    },

    // 监听键盘
    keyDown(event) {
      const videoIndex = this.selectedVideoIndex
      if (videoIndex === 0) {
        if (event.key === ' ') {
          this.isPlaying = !this.isPlaying
          if (this.isPlaying) {
            for (let i = 0; i < this.selectedVideos.length; i++) {
              this.$refs[`videoPlayer${i+1}`].player.pause()
            }
          } else {
            for (let i = 0; i < this.selectedVideos.length; i++) {
              this.$refs[`videoPlayer${i+1}`].player.play()
            }
          }
        }
      } else {
        if (event.key === ' ') {
          if (this.$refs[`videoPlayer${videoIndex}`].player.paused() === false) {
            this.$refs[`videoPlayer${videoIndex}`].player.pause()
          } else {
            this.$refs[`videoPlayer${videoIndex}`].player.play()
          }
        }
      }
      
    },

    playNextVideo(playerNumber) {
      // 对应播放器的索引+1
      this.order[playerNumber-1]++

      // 拿到下一个播放的视频对象
      this.nextVideo[playerNumber] = this.selectedVideos[playerNumber-1].parent[2].children[this.order[playerNumber-1]]

      // 通过nextVideo请求播放器播放
      this.$request.get("/video/showVideo", {
        params: {
          videoPath: this.nextVideo[playerNumber].videoPath,
          videoName: this.nextVideo[playerNumber].label
        }
      })
      this.playerOptions[playerNumber-1].sources[0].src = "http://localhost:8888/video/showVideo?videoPath=" + this.nextVideo[playerNumber].videoPath + "&" + "videoName=" + this.nextVideo[playerNumber].label
    },

    // 播放器时间触发函数
    timeUpdate(videoIndex) {
      // 如果当前播放器没有播放内容则返回
      if (this.playerOptions[videoIndex-1].sources[0].src === '') {
        return
      }

      // 如果播放全部视频，将当前进度置为每个视频的进度
      if (this.selectedVideoIndex === 0) {
        this.currentTime = Math.floor(this.$refs[`videoPlayer${videoIndex}`].player.currentTime())
        this.progress = Math.floor(this.$refs[`videoPlayer${videoIndex}`].player.currentTime())
      }

      // 如果播放单个视频，将进度条最大进度置为当前视频的总时长，将当前进度置为当前视频的进度
      if (this.selectedVideoIndex === videoIndex) {
        this.currentTime = Math.floor(this.$refs[`videoPlayer${videoIndex}`].player.currentTime())
        this.maxProgress = Math.floor(this.$refs[`videoPlayer${videoIndex}`].player.duration())
        this.progress = Math.floor(this.$refs[`videoPlayer${videoIndex}`].player.currentTime())
      }

      // 随着视频播放动态显示rect
      this.showRect()
    },

    // 进度条触发函数
    sliderChange() {
      // 点击进度条调整视频进度
      this.currentTime = this.progress  // 存储当前时间

      if (this.selectedVideoIndex === 0) {
        for (let i = 0; i < this.selectedVideos.length; i++) {
          this.$refs[`videoPlayer${i+1}`].player.currentTime(this.progress)
        }
      } else {
        // 选择单个视频则只需要将视频当前时间置为进度条的进度即可
        this.$refs[`videoPlayer${this.selectedVideoIndex}`].player.currentTime(this.progress)
      }
      // 随着视频播放动态显示rect
      this.showRect()
    },
    
    // 格式化，工具类
    timeToSeconds(time) {
      // 将时分秒格式转化为秒数
      const parts = time.split(":")
      return +parts[0] * 3600 + +parts[1] * 60 + +parts[2]
    },
    formatTooltip(time) {
      // 将进度值（秒数）格式化为 "hh:mm:ss"
      const hours = Math.floor(time / 3600)
      const minutes = Math.floor((time % 3600) / 60)
      const seconds = Math.floor(time % 60)
      return `${this.formatTimeUnit(hours)}:${this.formatTimeUnit(minutes)}:${this.formatTimeUnit(seconds)}`
    },
    formatTimeUnit(time) {
      // 辅助函数，将时间单位格式化为两位数
      return time.toString().padStart(2, '0')
    },

    ///////////////// 打标签和数据表格展示 /////////////////
    // 展示record
    showRecord() {
      if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        if (this.selectedPersonId === '全选') {
          const videoIds = []
          for (let i = 1; i <= this.selectedVideos.length; i++) {
            videoIds.push(this.nextVideo[i].id)
          }
          console.log(videoIds.join())
          this.$request.get("/groupRecord/getGroupRecord", {
            params: {
              videoIds: videoIds.join(),
              groupName: this.selectedVideos[0].parent[1].label,
              uid: this.user.id,
              labelType: this.finalSelectedLabelType
            }
          }).then(res => {
            this.recordData = res.data
            this.createRecordSegments(this.recordData)
          })
        } else {
          this.$request.get("/record/getRecord", {
            params: {
              videoName: this.videoNames[this.selectedVideoIndex-1],
              labelType: this.finalSelectedLabelType,
              uid: this.user.id,
              personId: this.selectedPersonId
            }
          }).then(res => {
            this.recordData = res.data
            this.createRecordSegments(this.recordData)
          })
        }
      }
    },

    // 创建recordSegments数组用于后续显示彩带
    createRecordSegments(recordData) {
      this.recordSegments = recordData.map(recordSegment => ({
        start: recordSegment.start,
        end: recordSegment.end,
        color: this.buttonColors[this.finalSelectedLabelType].find(item => item.label === recordSegment.label).color
      }))
    },

    // 显示彩带
    generateProcess() {
      if (this.selectedPersonId === '全选') {
        return this.recordSegments.map(recordSegment => {
          // 将数据库中的数据转化为秒数
          const startInSeconds = this.timeToSeconds(recordSegment.start)
          const endInSeconds = this.timeToSeconds(recordSegment.end)

          // 计算色块所占百分比
          const startPercentage = (startInSeconds / Math.max(...this.durations)) * 100
          const endPercentage = (endInSeconds / Math.max(...this.durations)) * 100
          const color = recordSegment.color
          return [startPercentage, endPercentage, { backgroundColor: color }]  // 要想多段显示不同的颜色，格式就是[开始，结束，{属性：属性值}]
        })
      } else {
        return this.recordSegments.map(recordSegment => {
          // 将数据库中的数据转化为秒数
          const startInSeconds = this.timeToSeconds(recordSegment.start)
          const endInSeconds = this.timeToSeconds(recordSegment.end)

          // 计算色块所占百分比
          const startPercentage = (startInSeconds / this.durations[this.selectedVideoIndex-1]) * 100
          const endPercentage = (endInSeconds / this.durations[this.selectedVideoIndex-1]) * 100
          const color = recordSegment.color
          return [startPercentage, endPercentage, { backgroundColor: color }]  // 要想多段显示不同的颜色，格式就是[开始，结束，{属性：属性值}]
        })
      }
    },

    // 标注
    annotation(label) {
      // 如果选择的是单视频标注
      if (this.selectedVideoIndex !== 0) {
        if (!this.selectedPersonId) {
          this.$message.error("请选择被标注者")
        } else if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
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
              this.translateXValue = (this.currentTime / this.durations[this.selectedVideoIndex-1]) * this.$refs.slider.$el.clientWidth
              this.showMark = true

              // 将当前选择的标签设为正在标注的标签
              this.currentAnnotationLabel = label

              // 创建新的标注
              this.annotationInProgress = {
                videoName: this.videoNames[this.selectedVideoIndex-1],
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
                  this.translateXValue = (this.currentTime / this.durations[this.selectedVideoIndex-1]) * this.$refs.slider.$el.clientWidth
                  this.showMark = true

                  this.annotationInProgress.end = currentTime  // 更新正在标注的数据的end 
                  this.saveAnnotation(this.annotationInProgress)  // 存入数据库
                  this.currentAnnotationLabel = label // 将当前选择的标签设为正在标注的标签

                  // 创建新的标注
                  this.annotationInProgress = {
                    videoName: this.videoNames[this.selectedVideoIndex-1],
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
      }

      // 如果选择的是多视频标注
      if (this.selectedVideoIndex === 0) {
        const videoIds = []
        for (let i = 1; i <= this.selectedVideos.length; i++) {
          videoIds.push(this.nextVideo[i].id)
        }
        if (!this.selectedPersonId) {
          this.$message.error("请选择被标注者")
        } else if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
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
              this.translateXValue = (this.currentTime / Math.max(...this.durations)) * this.$refs.slider.$el.clientWidth
              this.showMark = true

              // 将当前选择的标签设为正在标注的标签
              this.currentAnnotationLabel = label
              // 创建新的标注
              this.annotationInProgress = {
                videoIds: videoIds.join(),
                groupName: this.selectedVideos[0].parent[1].label,
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
                  this.translateXValue = (this.currentTime / Math.max(...this.durations)) * this.$refs.slider.$el.clientWidth
                  this.showMark = true

                  this.annotationInProgress.end = currentTime  // 更新正在标注的数据的end 
                  this.saveAnnotation(this.annotationInProgress)  // 存入数据库
                  this.currentAnnotationLabel = label // 将当前选择的标签设为正在标注的标签

                  // 创建新的标注
                  this.annotationInProgress = {
                    videoIds: videoIds.join(),
                    groupName: this.selectedVideos[0].parent[1].label,
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
      }
    },

    // 保存标记
    saveAnnotation(annotation) {
      // 将标注数据保存到数据库
      if (this.selectedPersonId === '全选') {
        this.$request.post("/groupRecord/createGroupRecord", annotation).then(() => {
          // 刷新record
          this.showRecord()
        })
      } else {
        this.$request.post("/record/createRecord", annotation).then(() => {
          // 刷新record
          this.showRecord()
        })
      }
    },

    // 取消标注
    cancelAnnotation() {
      if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.showMark = false
        this.annotationInProgress = null
      }
    },

    // 更新record
    updateRecord(index, row) {
      if (this.selectedPersonId === '全选') {
        if (this.isEditingRow === null) {  // 如果是第一次点击编辑，开启编辑模式
          // 赋初值
          this.isEditingRow = index
          this.firstStart = row.start
          this.firstEnd = row.end
          this.firstLabel = row.label
        } else if (this.isEditingRow === index) {  // 如果是第二次点击编辑，即可把修改的数据保存
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
            console.log(row)
            this.$request.post("/groupRecord/updateGroupRecord", {
              id: row.id,
              videoIds: row.videoIds,
              groupName: row.groupName,
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
      } else {
        if (this.isEditingRow === null) {  // 如果是第一次点击编辑，开启编辑模式
          // 赋初值
          this.isEditingRow = index
          this.firstStart = row.start
          this.firstEnd = row.end
          this.firstLabel = row.label
        } else if (this.isEditingRow === index) {  // 如果是第二次点击编辑，即可把修改的数据保存
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

    // 检查更新数据合法性
    isGood(row) {
      // 备份原始 recordSegments
      let originalSegments = [...this.recordSegments]

      // 找到当前行对应originalSegments里的下标
      const rowIndex = originalSegments.findIndex(segment => {
        return segment.start === this.firstStart && segment.end === this.firstEnd
      })

      // 将当前行对应的片段去掉
      if (rowIndex !== -1) {
        originalSegments = originalSegments.filter((segment, index) => index !== rowIndex)
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

    // 删除record
    deleteRecord(row) {
      if (this.selectedPersonId === '全选') {
        if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
        } else {
          // 根据表格当前行删除record，所以不需要别的参数，直接根据id删除不会有bug
          this.$request.delete("/groupRecord/deleteGroupRecord/" + row.id).then(() => {
            // 删除完，刷新一遍record
            this.showRecord()
          })
        }
      } else {
        if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
        } else {
          // 根据表格当前行删除record，所以不需要别的参数，直接根据id删除不会有bug
          this.$request.delete("/record/deleteRecord/" + row.id).then(() => {
            // 删除完，刷新一遍record
            this.showRecord()
          })
        }
      }
    },

    // 点击跳转对应时间
    jumpToTime(time) {
      this.$refs[`videoPlayer${this.selectedVideoIndex}`].player.currentTime(this.timeToSeconds(time))
    },

    // 获取表格中标签的展示颜色
    getLabelColor(label) {
      // 根据label设置this.buttonColors
      return this.buttonColors[this.finalSelectedLabelType].find(item => item.label === label).color
    },

    ///////////////// 画布和画框 /////////////////
    // 绑定监听函数
    bindCanvasOn(videoIndex) {
      this.canvas[videoIndex].on('mouse:down', this.mouseDown)
      this.canvas[videoIndex].on('mouse:up', this.mouseUp)
      this.canvas[videoIndex].on('object:moving', this.objectMoving)
      this.canvas[videoIndex].on('object:scaling', this.objectScaling)
      this.canvas[videoIndex].on('object:rotating', this.objectRotating)
      this.canvas[videoIndex].on('selection:created', this.selectionCreated)
      this.canvas[videoIndex].on('selection:updated', this.selectionUpdated)
    },

    // 开始画框
    startDraw() {
      const videoIndex = this.selectedVideoIndex
      if (this.selectedVideos.length === 0) {
        this.$message.error("还未选择视频")
      } else if (videoIndex === 0) {
        this.$message.error("请选择单个视频画框")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (!this.selectedPersonId) {
        this.$message.error("请选择被标注者")
      } else if (!this.selectedRect) {
        this.$message.error("请选择对应被标注者的矩形框")
      } else if (this.selectedPersonId !== this.selectedRect && this.selectedRect !== '全选') {
        this.$message.error("请选择相同的被标注者")
      } else {
        if (this.$refs[`videoPlayer${videoIndex}`].player.paused() === false) {
          this.$message.error("请先暂停视频才能开始画框")
        } else {
          // 切换startDrawMode
          this.startDrawMode = !this.startDrawMode

          if (this.startDrawMode === true) {  // 开始画框
            // 将当前选择的播放器的画布置为不可穿透  
            document.querySelector(`.canvas-container${videoIndex}`).style.pointerEvents = 'auto'

            console.log("--------开始画框时this.canvas--------")
            console.log(this.canvas)
            console.log("--------开始画框时this.canvas--------")
            
            // 监听函数
            this.bindCanvasOn(videoIndex)
            // document.addEventListener('keydown', this.deleteRect)
          } else {  // 结束画框
            // 将当前选择的播放器的画布置为可穿透  
            document.querySelector(`.canvas-container${videoIndex}`).style.pointerEvents = 'none'

            // 每次结束画框后取消所有框的选中状态，否则会出bug
            this.canvas[videoIndex].discardActiveObject()
            this.canvas[videoIndex].renderAll()

            // 创建或更新face
            for (const personId in this.rects[videoIndex]) {
              for (const time in this.rects[videoIndex][personId]) {
                if (this.formatTooltip(this.currentTime) === time) {  // 将当前time的所有框传给后台
                  const rect = this.rects[videoIndex][personId][time]
                  this.$request.post("/face/saveOrUpdateFace", {
                    videoName: this.videoNames[videoIndex-1],
                    personId: rect.personId,
                    uid: rect.uid,
                    time: this.formatTooltip(this.currentTime),
                    x0: (rect.aCoords.tl.x / this.canvas[videoIndex].width) * 100,
                    y0: (rect.aCoords.tl.y / this.canvas[videoIndex].height) * 100,
                    x1: (rect.aCoords.br.x / this.canvas[videoIndex].width) * 100,
                    y1: (rect.aCoords.br.y / this.canvas[videoIndex].height) * 100,
                  }).then(() => {
                    this.showRect()
                  })
                }
              }
            }
          }
        }
      }
    },

    // 获取矩形框数据 
    async getRect() {
      // 获取播放器下标
      const videoIndex = this.selectedVideoIndex
      
      if (videoIndex !== 0) {  // 如果播放器不是全选
        if (this.selectedRect === '全选') {  // 显示全部person_id对应的矩形框
          // 设置参数params
          const params = {
            videoName: this.videoNames[videoIndex-1],
            uid: this.user.id,
          }

          // 从数据库获取face表的数据，并按照time由小到大排列，赋值给this.faces
          // 得保证在showRect中先执行完getRect再执行下述代码，所以用异步
          const res = await this.$request.get("/face/getFace", { params })

          // 排序
          res.data.sort((a, b) => {
            return a.time.localeCompare(b.time)
          })
          this.faces[this.selectedVideoIndex] = res.data
        } else {  // 显示person_id对应的矩形框
          // 设置参数params
          const params = {
            videoName: this.videoNames[videoIndex-1],
            uid: this.user.id,
            personId: this.selectedRect
          }

          // 从数据库获取face表的数据，并按照time由小到大排列，赋值给this.faces
          // 得保证在showRect中先执行完getRect再执行下述代码，所以用异步
          const res = await this.$request.get("/face/getFaceByPersonId", { params })

          // 排序
          res.data.sort((a, b) => {
            return a.time.localeCompare(b.time)
          })
          this.faces[this.selectedVideoIndex] = res.data
        }
      } else {  // 如果播放器是全选
        if (this.selectedRect === '全选') {
          for (let i = 1; i <= 4; i++) {
            // 设置参数params
            const params = {
              videoName: this.videoNames[i-1],
              uid: this.user.id,
            }

            // 从数据库获取face表的数据，并按照time由小到大排列，赋值给this.faces
            // 得保证在showRect中先执行完getRect再执行下述代码，所以用异步
            const res = await this.$request.get("/face/getFace", { params })

            // 排序
            res.data.sort((a, b) => {
              return a.time.localeCompare(b.time)
            })
            this.faces[i] = res.data
          }
        }
      }
    },

    // 展示矩形框数据
    async showRect() {
      // 获取播放器下标
      const videoIndex = this.selectedVideoIndex
      // 获取当前时间
      const currentTime = this.formatTooltip(this.currentTime)

      // 先更新一下this.faces
      await this.getRect()

      if (videoIndex !== 0) {  // 如果播放器不是全选
        // 清除画布上的所有对象
        this.canvas[videoIndex].getObjects().forEach(obj => {
          this.canvas[videoIndex].remove(obj)
        })

        // 遍历所有的person_id，这里拿this.personIds获取所有person_id
        for (const personId of this.personIds[videoIndex]) {
          // 拿到距离最近的时间点的框的信息closestFace
          let closestFace = null
          for (const face of this.faces[videoIndex]) {
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
              left: closestFace.x0 * this.canvas[videoIndex].width / 100,
              top: closestFace.y0 * this.canvas[videoIndex].height / 100,
              width: (closestFace.x1 - closestFace.x0) * this.canvas[videoIndex].width / 100,
              height: (closestFace.y1 - closestFace.y0) * this.canvas[videoIndex].height / 100,
              fill: 'transparent',
              stroke: this.colorMap[videoIndex][closestFace.personId],
              strokeWidth: 0.5,
              strokeUniform: true,
              personId: closestFace.personId,
              uid: closestFace.uid,
              time: closestFace.time
            })
            this.canvas[videoIndex].add(rect)
          }
        }
        this.canvas[videoIndex].renderAll()
      } else {  // 如果播放器是全选
        if (this.selectedRect === '全选') {
          for (let i = 1; i <= this.selectedVideos.length; i++) {
            // 清除画布上的所有对象
            this.canvas[i].getObjects().forEach(obj => {
              this.canvas[i].remove(obj)
            })

            // 遍历所有的person_id，这里拿this.personIds获取所有person_id
            for (const personId of this.personIds[i]) {
              // 拿到距离最近的时间点的框的信息closestFace
              let closestFace = null
              for (const face of this.faces[i]) {
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
                  left: closestFace.x0 * this.canvas[i].width / 100,
                  top: closestFace.y0 * this.canvas[i].height / 100,
                  width: (closestFace.x1 - closestFace.x0) * this.canvas[i].width / 100,
                  height: (closestFace.y1 - closestFace.y0) * this.canvas[i].height / 100,
                  fill: 'transparent',
                  stroke: this.colorMap[i][closestFace.personId],
                  strokeWidth: 0.5,
                  strokeUniform: true,
                  personId: closestFace.personId,
                  uid: closestFace.uid,
                  time: closestFace.time
                })
                this.canvas[i].add(rect)
              }
            }
            this.canvas[i].renderAll()
          } 
        }
      }
    },

    // 落笔
    mouseDown(event) {
      if (!this.isDrawing) {
        // 设置正在画框标志
        this.isDrawing = true
        // 获取鼠标起始点
        this.startPoint = this.canvas[this.selectedVideoIndex].getPointer(event.e)
      }
    },

    // 抬笔
    mouseUp(event) {
      const videoIndex = this.selectedVideoIndex  // 播放器下标
      const currentTime = this.formatTooltip(this.currentTime)  // 当前时间

      if (this.isDrawing) {
        // 设置结束画框标志
        this.isDrawing = false
        // 获取鼠标结束点
        this.endPoint = this.canvas[videoIndex].getPointer(event.e)

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
        if (left + width > this.canvas[videoIndex].width) {
          width = this.canvas[videoIndex].width - left
        }
        if (top + height > this.canvas[videoIndex].height) {
          height = this.canvas[videoIndex].height - top
        }
        
        console.log("--------抬笔时 矩形创建前 this.rects--------")
        console.log(this.rects)
        console.log("--------抬笔时 矩形创建前 this.rects--------")
        // 如果this.rects当前播放器的当前person_id的当前时间里没有数据，则赋值
        if (!this.rects[videoIndex][this.selectedPersonId][currentTime]) {
          // 清除对应播放器画布上的所有矩形框
          this.canvas[videoIndex].getObjects().forEach(obj => {
            if (obj.personId === this.selectedPersonId) {
              this.canvas[videoIndex].remove(obj)
            }
          })

          // 创建矩形框对象
          this.rects[videoIndex][this.selectedPersonId][currentTime] = new fabric.Rect({
            left: left,
            top: top,
            width: width,
            height: height,
            fill: 'transparent',
            stroke: this.colorMap[videoIndex][this.selectedPersonId],
            strokeWidth: 0.5,
            strokeUniform: true,
            personId: this.selectedPersonId,
            uid: this.user.id,
          })
          console.log("--------抬笔时 矩形创建后 this.rects--------")
          console.log(this.rects)
          console.log("--------抬笔时 矩形创建后 this.rects--------")

          // 加入画布
          this.canvas[videoIndex].add(this.rects[videoIndex][this.selectedPersonId][currentTime])
          
          console.log("--------加入画布this.canvas--------")
          console.log(this.canvas)
          console.log("--------加入画布this.canvas--------")

          // 画完之后将不是当前播放器以及当前person_id以及当前时间对应的this.rects的rect对象remove
          // for (const time in this.rects[videoIndex][this.selectedPersonId]) {
          //   if (time !== currentTime) {
          //     const rect = this.rects[videoIndex][this.selectedPersonId][time]
          //     if (rect) {
          //       this.canvas[videoIndex].remove(rect)
          //     }
          //   }
          // }
          this.canvas[videoIndex].renderAll()  // 重新渲染
        }
      }
    },

    // 鼠标操作矩形框改变时触发的函数
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
      const videoIndex = this.selectedVideoIndex
      // 以免出现bug，重新赋值一遍
      this.selectedPersonId = event.selected[0].personId

      // 将当前选择的对象，给予他新的时间属性
      this.rects[videoIndex][event.selected[0].personId][this.formatTooltip(this.currentTime)] = event.selected[0]
    },
    selectionUpdated(event) {  // 选择不同的对象时触发
      const videoIndex = this.selectedVideoIndex
      // 以免出现bug，重新赋值一遍
      this.selectedPersonId = event.selected[0].personId
      
      // 将当前选择的对象，给予他新的时间属性
      this.rects[videoIndex][event.selected[0].personId][this.formatTooltip(this.currentTime)] = event.selected[0]
    },

    // 删除矩形框
    // deleteRect(event) {
    //   if (event.key === 'd' && this.canvas[this.selectedVideoPlayer.index-1].getActiveObject()) {
    //     // 拿到当前选择的矩形框对象
    //     const selectedObject = this.canvas[this.selectedVideoPlayer.index-1].getActiveObject()

    //     // 判断保证只能删除对应的person_id的矩形框
    //     if (selectedObject.personId !== this.selectedPersonId) {
    //       this.$message.error('不能删除不对应被标注者的矩形框')
    //     } else {
    //       // 从画布中先移除选中矩形框
    //       this.canvas[this.selectedVideoPlayer.index-1].remove(selectedObject)
    //       this.canvas[this.selectedVideoPlayer.index-1].renderAll()

    //       // 删除this.rects和this.faces
    //       delete this.rects[selectedObject.personId][this.formatTooltip(this.currentTime)]
    //       this.faces = this.faces.filter(face => {
    //         return !(face.personId === selectedObject.personId && face.time === this.formatTooltip(this.currentTime))
    //       })

    //       // 删除后台数据，如果在数据库没有对应的time会执行这个请求但是删不掉，无所谓
    //       this.$request.delete("/face/deleteFace", {
    //         params: {
    //           videoName: this.selectedVideoPlayer.video.videoName,
    //           personId: selectedObject.personId,
    //           uid: selectedObject.uid,
    //           time: this.formatTooltip(this.currentTime)
    //         }
    //       })
    //     }
    //   }       
    // }, 
    
  }
}
</script>


<style scoped>
  .video-players-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    /* gap: 5px; */
  }
  .video-player {
    width: 100%;
    height: 100%;
    box-sizing: border-box;  /* 确保边距不会增加播放器的宽度 */
  }
  .video-player1 {
    position: relative;
    width: 100%;
    height: 100%;
    opacity: 1;
    z-index: 1;
    box-sizing: border-box;  /* 确保边距不会增加播放器的宽度 */
  }
  .video-player2 {
    position: relative;
    width: 100%;
    height: 100%;
    opacity: 1;
    z-index: 1;
    box-sizing: border-box;  /* 确保边距不会增加播放器的宽度 */
  }
  .video-player3 {
    position: relative;
    width: 100%;
    height: 100%;
    opacity: 1;
    z-index: 1;
    box-sizing: border-box;  /* 确保边距不会增加播放器的宽度 */
  }
  .video-player4 {
    position: relative;
    width: 100%;
    height: 100%;
    opacity: 1;
    z-index: 1;
    box-sizing: border-box;  /* 确保边距不会增加播放器的宽度 */
  }
  .canvas-container1 {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 2;
    pointer-events: none;
  }
  .canvas-container2 {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 2;
    pointer-events: none;
  }
  .canvas-container3 {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 2;
    pointer-events: none;
  }
  .canvas-container4 {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 2;
    pointer-events: none;
  }
</style>