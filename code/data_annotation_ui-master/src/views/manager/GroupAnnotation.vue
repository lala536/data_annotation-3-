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

          <el-link style="margin-left: 25px;" :underline="false">音轨选择：</el-link>
          <el-select v-model="selectedSound" @change="showSound" clearable :disabled="isSelectedSoundDisabled" style="width: 100px;">
            <el-option
              v-for="sound in soundOptions"
              :key="sound"
              :label="sound"
              :value="sound">
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

        <!-- 播放器，最多四个 -->
        <div v-if="showVideoPlayer" class="video-players-container">
          <div v-if="selectedVideos.length >= 1" class="video-player">
            <div style="position: relative;">
              <div style="z-index: 1;">
                <video-player class="video-player vjs-custom-skin"
                  ref="videoPlayer1"
                  :playsinline="true"
                  :options="playerOptions[0]"
                  @timeupdate="handleTimeUpdate(1)"
                  @ended="playNextVideo(1)"
                ></video-player>
              </div>
            </div>
          </div>

          <div v-if="selectedVideos.length >= 2" class="video-player">
            <div style="position: relative;">
              <div style="z-index: 1;">
                <video-player class="video-player vjs-custom-skin"
                  ref="videoPlayer2"
                  :playsinline="true"
                  :options="playerOptions[1]"
                  @timeupdate="handleTimeUpdate(2)"
                  @ended="playNextVideo(2)"
                ></video-player>
              </div>
            </div>
          </div>

          <div v-if="selectedVideos.length >= 3" class="video-player">
            <div style="position: relative;">
              <div style="z-index: 1;">
                <video-player class="video-player vjs-custom-skin"
                  ref="videoPlayer3"
                  :playsinline="true"
                  :options="playerOptions[2]"
                  @timeupdate="handleTimeUpdate(3)"
                  @ended="playNextVideo(3)"
                ></video-player>
              </div>
            </div>
          </div>

          <div v-if="selectedVideos.length >= 4" class="video-player">
            <div style="position: relative;">
              <div style="z-index: 1;">
                <video-player class="video-player vjs-custom-skin"
                  ref="videoPlayer4"
                  :playsinline="true"
                  :options="playerOptions[3]"
                  @timeupdate="handleTimeUpdate(4)"
                  @ended="playNextVideo(4)"
                ></video-player>
              </div>
            </div>
          </div>
        </div>
          
        <!-- 进度条 -->
        <div v-show="showSlider" style="margin-top: 5px;">
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
      selectedVideos: null,  // 使用复选框获取选中的节点map完的信息
      cameraSize: null,  // 192.168.0.101这样的摄像头数量（有多少就播放多少视频）
      videoPath: null,  // 视频路径
      videoName: null,  // 视频名称
      videoPaths: [],  // 多视频路径
      videoNames: [],  // 多视频名称
      uniqueKey: 0,  // 确保每次选择视频都强制渲染新的视频播放器，以免出现bug
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
      showVideoPlayer: false, 
      showSlider: false,  // 是否显示进度条
      showMark: false,  // 打标签的开始标记
      translateXValue: 0,  // 一个偏移量
      annotationInProgress: null,  // 用于存储当前正在标注的数据
      currentAnnotationLabel: null,  // 用于存储当前标注的标签
      isEditingRow: null,  // 是否正在修改
      firstStart: null,  // 初始的开始时间
      firstEnd: null,  // 初始的结束时间
      firstLabel: null,  // 初始的标签
      isPlaying: false,  // 是否播放
      isSearchDisabled: false,  // 是否允许操作下拉框
      isSelectedPersonIdDisabled: false,
      currentTime: null,  // 当前时间，单位是秒
      duration: null,  // 总时长，单位是秒
      index: null,
      videoIndex: [],
      soundOptions: [],
      selectedSound: null,  // 选择音轨
      playerOptions: [{  // 视频播放器选项
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
      },{  // 视频播放器选项
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
      },{  // 视频播放器选项
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
      },{  // 视频播放器选项
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
      }],
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
    // 在组件挂载时添加统一的键盘事件监听器
    document.addEventListener('keydown', this.keyDown);
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
    async loadLabel() {
      // 获取所有label数据
      const res = await this.$request.get("/label/getAllLabel", {
        params: {
          isGroup: 1,
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
      if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.videoDrawerVisible = true
      }
    },
    async chooseVideo() {
      // 使用复选框获取选中的节点
      const selectedNodes = this.$refs.videoTree.getCheckedNodes()

      // 过滤出叶子节点并map出自己想要的信息
      this.selectedVideos = selectedNodes
      .filter(node => !node.children || node.children.length === 0)
      .map(node => ({ 
        videoPath: node.videoPath, 
        videoName: node.label, 
        parent: node.parent,
        videoIndex: node.parent[2].children.findIndex(child => child.label === node.label),
      }))

      // 检查所选视频是否来自同一小组
      const sameGroup = this.selectedVideos.every(video => video.parent[1].label === this.selectedVideos[0].parent[1].label)

      // 每个摄像头下只能选择最多一个视频
      const uniqueCameras = new Set(this.selectedVideos.map(video => video.parent[2].label))
      const isUniqueCamera = [...uniqueCameras].every(camera => this.selectedVideos.filter(video => video.parent[2].label === camera).length <= 1)

      if (!sameGroup) {
        this.$message.error("请选择同一小组的视频")
      } else if (!isUniqueCamera) {
        this.$message.error("每个摄像头下只能选择最多一个视频")
      } else {  // 是同一小组
        this.videoDrawerVisible = false
        
        // 清空当前选择
        this.videoPaths = []
        this.videoNames = []
        
        // 为this.videoIndex赋初值
        for (let i = 0; i < this.selectedVideos.length; i++) {
          this.videoIndex[i] = this.selectedVideos[i].videoIndex
        }

        // 将视频信息存入数组
        for(const node of this.selectedVideos) {
          this.videoPaths.push(node.videoPath)
          this.videoNames.push(node.videoName)
        }

        // 音轨下拉框赋值
        for (let i = 0; i < this.selectedVideos.length; i++) {
          this.soundOptions[i] = i + 1
        }

        // 如果选择了标签组，则可以直接切换视频
        if (this.checkedLabelType != null) {
          this.showVideoPlayer = false
          this.showSlider = false
          await this.showVideo()
          // this.showRecord()
        } else {
          this.$message.error("请先选择标签")
        }
      }
      console.log(this.selectedVideos)
    },
    getLabelType() {
      if (this.isEditingRow !== null) {
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

      if (this.videoPaths.length === 0 && this.videoNames.length === 0 && !this.finalCheckedLabelType) {
        this.$message.error("请选择视频和标签组")
      } else if (this.videoPaths.length !== 0 && this.videoNames.length !== 0 && !this.finalCheckedLabelType) {
        this.$message.error("请选择标签组")
      } else if (this.videoPaths.length === 0 && this.videoNames.length === 0 && this.finalCheckedLabelType) {
        this.$message.error("请选择视频")
      } else if (this.videoPaths.length !== 0 && this.videoNames.length !== 0 && this.finalCheckedLabelType) {
        await this.showVideo()
        // this.showRecord()
      }
    },
    // async showVideo2() {
    //   const promises = []
    //   for (let i = 0; i < this.selectedVideos.length; i++) {
    //     const promise = this.loadVideo(i)
    //     promises.push(promise)
    //   }

    //   await Promise.all(promises)
    //   // 全部加载完毕才显示视频和进度条
    //   this.showVideoPlayer = true
    //   this.showSlider = true
    // },
    // async loadVideo2(i) {
    //   this.$request.get("/video/showVideo", {
    //     params: {
    //       videoPath: this.videoPaths[i],
    //       videoName: this.videoNames[i]
    //     }
    //   })
    //   this.playerOptions[i].sources[0].src = "http://localhost:8888/video/showVideo?videoPath=" + this.videoPaths[i] + "&" + "videoName=" + this.videoNames[i]
    //   return Promise.resolve()
    // },
    async showVideo() {
      for (let i = 0; i < this.selectedVideos.length; i++) {
        this.loadVideo(i)
      }

      // 全部加载完毕才显示视频和进度条
      this.showVideoPlayer = true
      this.showSlider = true
    },
    loadVideo(i) {
      this.$request.get("/video/showVideo", {
        params: {
          videoPath: this.videoPaths[i],
          videoName: this.videoNames[i]
        }
      })
      this.playerOptions[i].sources[0].src = "http://localhost:8888/video/showVideo?videoPath=" + this.videoPaths[i] + "&" + "videoName=" + this.videoNames[i]
    },
    keyDown(event) {
      try {
        if (event.key === ' ' && this.showVideoPlayer) {
          this.isPlaying = !this.isPlaying
          // 根据播放状态来控制播放/暂停
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
      } catch (error) {
        console.log(error)
      }
    },
    handleTimeUpdate(playerNumber) {
      const values = []
      // 四个视频的总时长数组
      for (let i = 0; i < this.selectedVideos.length; i++) {
        values[i] = this.$refs[`videoPlayer${i+1}`].player.duration()
      }

      // 获取最大总时长赋值给进度条
      this.maxProgress = Math.floor(Math.max(...values))

      // 将当前播放器的进度传给进度条
      this.progress = Math.floor(this.$refs[`videoPlayer${playerNumber}`].player.currentTime())
    },
    handleSliderChange() {
      this.currentTime = this.progress
      for (let i = 0; i < this.selectedVideos.length; i++) {
        this.$refs[`videoPlayer${i+1}`].player.currentTime(this.progress)
      }
    },
    playNextVideo(playerNumber) {
      // 对应播放器的索引+1
      this.videoIndex[playerNumber-1]++

      // 拿到下一个播放的视频对象
      const nextVideo = this.selectedVideos[playerNumber-1].parent[2].children[this.videoIndex[playerNumber-1]]

      // 通过nextVideo请求播放器播放
      this.$request.get("/video/showVideo", {
        params: {
          videoPath: nextVideo.videoPath,
          videoName: nextVideo.label
        }
      })
      this.playerOptions[playerNumber-1].sources[0].src = "http://localhost:8888/video/showVideo?videoPath=" + nextVideo.videoPath + "&" + "videoName=" + nextVideo.label
    },
    showSound() {
      console.log(this.$refs.videoPlayer1)
      // this.$refs.videoPlayer1.options.muted = true
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
    jumpToTime(time) {
      this.$refs.videoPlayer.player.currentTime(this.timeToSeconds(time))
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
  .video-players-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 5px;
  }
  .video-players-container .video-player {
    width: 100%;
    height: 100%;
    box-sizing: border-box;  /* 确保边距不会增加播放器的宽度 */
  }
</style>

