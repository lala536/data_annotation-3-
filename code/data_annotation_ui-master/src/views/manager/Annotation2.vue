<template>
  <div>
    <el-container style="height: 89vh">
      <!-- 标注部分 -->
      <el-main style="box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); width: 66%">
        <!-- 顶部 -->
        <div style="margin-bottom: 10px;">
          <!-- 选择视频按钮 -->
          <el-button type="primary" plain @click="getVideo" :disabled="isVideoDisabled">视频</el-button>
          <!-- 选择标签组按钮 -->
          <el-button type="primary" plain @click="getLabelType">标签</el-button>

          <!-- 选择播放器 -->
          <el-link style="margin-left: 10px;" :underline="false">播放器&nbsp;</el-link>
          <el-select v-model="selectedVideoIndex" @change="chooseVideoPlayer(selectedVideoIndex)" style="width: 70px;">
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
          <el-link style="margin-left: 10px;" :underline="false">被标注者&nbsp;</el-link>
          <el-select v-model="selectedPersonId" @change="showRecord" clearable style="width: 83px;">
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

          <!-- 根据person_id查询Rect -->
          <el-link style="margin-left: 10px;" :underline="false">矩形框&nbsp;</el-link>
          <el-select v-model="selectedRect" @change="showRect" clearable style="width: 83px;">
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

          <!-- 根据person_id查询text -->
          <el-link style="margin-left: 10px;" :underline="false">文本&nbsp;</el-link>
          <el-select v-model="selectedText" @change="showText" clearable style="width: 83px;">
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

          <el-link v-show="showSelectAnnotator" style="margin-left: 10px;" :underline="false">标注者&nbsp;</el-link>
          <!-- 根据uid查询Record和Rect -->
          <el-select v-show="showSelectAnnotator" v-model="selectedUsername" @change="showRecordAndRectByUid" style="width: 70px;">
            <el-option
              v-for="username in usernames"
              :key="username"
              :label="username"
              :value="username">
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
          <el-dialog title="标签" :visible.sync="labelTypeDialog" width="50%" @close="closeLabelType">
            <div style="margin-bottom: 10px;">
              个人标签：
            </div>
            <el-radio-group v-model="selectedSingleLabelType" style="display: flex; flex-direction: column;"><!-- 而 flex-direction: column; 则指定子元素（即 <el-radio> 组件）应该垂直排列（即从上到下）。这样，单选按钮组中的每个单选按钮都会垂直堆叠在一起。-->
              <div v-for="singleLabelType in singleLabelTypes" :key="singleLabelType" style="margin-bottom: 10px; display: flex; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-radio :label="singleLabelType" style="width: 100%; flex: 1;">
                  {{ singleLabelType }}
                </el-radio>
                <div>
                  <el-button v-for="item in buttonColors[singleLabelType]" :key="item.label" :style="{ backgroundColor: item.color }">{{ item.label }}</el-button>
                </div>
              </div>
            </el-radio-group>

            <div style="margin-bottom: 10px;">
              小组标签：
            </div>
            <el-radio-group v-model="selectedMultiLabelType" style="display: flex; flex-direction: column;">
              <div v-for="multiLabelType in multiLabelTypes" :key="multiLabelType" style="margin-bottom: 10px; display: flex; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-radio :label="multiLabelType" style="width: 100%; flex: 1;">
                  {{ multiLabelType }}
                </el-radio>
                <div>
                  <el-button v-for="item in buttonColors[multiLabelType]" :key="item.label" :style="{ backgroundColor: item.color }">{{ item.label }}</el-button>
                </div>
              </div>
            </el-radio-group>

            <div slot="footer" class="dialog-footer">
              <el-button @click="getAlterLabelType">更改标签</el-button>
              <el-button @click="clearLabelType">清 空</el-button>
              <el-button @click="closeLabelType">取 消</el-button>
              <el-button type="primary" @click="chooseLabelType">选 择</el-button>
            </div>
          </el-dialog>


            <el-dialog title="更改标签" :visible.sync="alterLabelTypeDialog" width="50%" @close="closeAlterLabelType">
            <div style="margin-bottom: 10px;">
              <el-button type='primary' >个人标签</el-button>
              <el-button type='success' >小组标签</el-button>
              <el-button type='primary' style="float: right;margin-left: 10px;" @click="addPersonalLabelType">新增个人标签</el-button>
            </div>
            <div  style="display: flex; flex-direction: column;" >
              <!-- <div v-for="singleLabelType in singleLabelTypes" :key="singleLabelType" style="margin-bottom: 10px; display: flex; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                {{singleLabelType}}S
                <div>
                  <el-button v-for="item in buttonColors[singleLabelType]" :key="item.label" :style="{ backgroundColor: item.color }">{{ item.label }} {{item.code}}</el-button>
                </div>
              </div> -->
              <div v-show="showBasicLabel">    
                <input type="text" value="基本情绪标签" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px" >
                <input type="text" value="标签组描述信息" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-button type='success' style="float: right; margin-right: 10px;" @click="show1 = !show1">收起/展开</el-button>
                <el-button type='danger' style="float: right; margin-right: 10px;" @click="deleteBasicLabel()">   删除标签组</el-button>
              
             <div style="height: auto;border: 1px dashed #ccc;">
                <el-collapse-transition>
                  <div v-show="show1">
                  <div v-for="bsEmotion in baseEmotion" :key="bsEmotion.index">
                    <el-input   placeholder="标签名称"  style="width: 80px;" v-model="bsEmotion.label"></el-input>
                    <el-input   placeholder="标签描述信息" style="width: 200px;" v-model="bsEmotion.description"></el-input>
                    <el-input   placeholder="编码" style="width: 60px;" v-model="bsEmotion.code"></el-input>
                    <span class="block" style="margin-top: 10px; margin-left: 10px;">
                      <span class="demonstration">颜色选择器</span>
                      <el-color-picker v-model="bsEmotion.color" style=" margin-top: 10px;margin-right: 10px;margin-left: 10px;"></el-color-picker>
                    </span>
                    <el-switch
                        v-model="bsEmotion.enable"
                        active-color="#ff4949" 
                        inactive-color="#13ce66"
                        @change="changeEnable(bsEmotion.enable)">
                    </el-switch>
                    <el-button type="info" style="margin-right: 5px;">是否启用</el-button>
                    <el-button type="danger" @click="deleteLabelType(bsEmotion.labelType,bsEmotion.label,bsEmotion.id)">删除标签</el-button>
                  </div>
                  <el-button type="info" style="margin-top: 10px; margin-bottom: 10px; " @click="addBaseLabelType(baseEmotion.label)">添加标签</el-button>
                  </div>
                </el-collapse-transition>
             </div>
              </div> 
            <div style="margint-top: 10px;" v-show="showQingXvLabels">    
                <input type="text" value="情绪标签" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px" >
                <input type="text" value="标签组描述信息" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-button type='success' style="float: right; margin-right: 10px;margin-top: 10px;" @click="show2 = !show2">收起/展开</el-button>
                <el-button type='danger' style="float: right; margin-right: 10px;margin-top: 10px;" @click="deleteQingXvLabels">删除标签组</el-button>
             <div style="height: auto;" >
                <el-collapse-transition>
                <div v-show="show2">
                <div v-for="JXEmotion in EmotionQingXv" :key="JXEmotion.index" >
                  <el-input   placeholder="标签名称"  style="width: 80px;" v-model="JXEmotion.label"></el-input>
                  <el-input   placeholder="标签描述信息" style="width: 200px;" v-model="JXEmotion.description"></el-input>
                  <el-input   placeholder="编码" style="width: 60px;" :value="JXEmotion.code"></el-input>
                  <span class="block" style="margin-top: 10px; margin-left: 10px;">
                    <span class="demonstration">颜色选择器</span>
                    <el-color-picker v-model="JXEmotion.color" style=" margin-top: 10px;margin-right: 10px;margin-left: 10px;"></el-color-picker>
                  </span>
                  <el-switch
                      v-model="JXEmotion.enable"
                        active-color="#13ce66"
                        inactive-color="#ff4949"
                        @change="changeEnable(JXEmotion.enable)">
                  </el-switch>
                  <el-button type="info" style="margin-right: 5px;">是否启用</el-button>
                  <el-button type="danger" @click="deleteLabelType(JXEmotion.labelType,JXEmotion.label,JXEmotion.id)">删除标签</el-button>
                </div>
                <el-button type="info" style="margin-top: 10px; margin-bottom: 10px; " @click="addQingXvLabelType(EmotionQingXv.label)">添加标签</el-button>
                </div>
                </el-collapse-transition>
              </div>
            </div>
        
              <div style="margint-top: 10px;" v-show="showJiXingLabels">    
                <input type="text" value="极性标签" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px" >  
                <input type="text" value="标签组描述信息" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-button type='success' style="float: right; margin-right: 10px;margin-top: 10px;" @click="show3 = !show3">收起/展开</el-button>
                <el-button type='danger' style="float: right; margin-right: 10px;margin-top: 10px;" @click="deleteJiXingLabels">删除标签组</el-button>
              
              <div style="height: auto;" >
                <el-collapse-transition>
                <div v-show="show3">
                <div v-for="JXEmotion in EmotionJiXing" :key="JXEmotion.index" >
                  <el-input   placeholder="标签名称"  style="width: 80px;" v-model="JXEmotion.label"></el-input>
                  <el-input   placeholder="标签描述信息" style="width: 200px;" v-model="JXEmotion.description"></el-input>
                  <el-input   placeholder="编码" style="width: 60px;" :value="JXEmotion.code"></el-input>
                  <span class="block" style="margin-top: 10px; margin-left: 10px;">
                    <span class="demonstration">颜色选择器</span>
                    <el-color-picker v-model="JXEmotion.color" style=" margin-top: 10px;margin-right: 10px;margin-left: 10px;"></el-color-picker>
                  </span>
                  <el-switch
                      v-model="JXEmotion.enable"
                      active-color="#13ce66"
                      inactive-color="#ff4949"
                      @change="changeEnable(JXEmotion.enable)">
                  </el-switch>
                  <el-button type="info" style="margin-right: 5px;">是否启用</el-button>
                  <el-button type="danger" @click="deleteLabelType(JXEmotion.labelType,JXEmotion.label,JXEmotion.id)">删除标签</el-button>
                </div>
                <el-button type="info" style="margin-top: 10px; margin-bottom: 10px; " @click="addJiXingLabelType(EmotionJiXing.label)">添加标签</el-button>
                </div>
                </el-collapse-transition>
              </div>
              </div>
              <div style="margint-top: 10px;" v-show="showYangLiLabels">    
                <input type="text" value="样例标签" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px" >
                <input type="text" value="标签组描述信息" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-button type='success' style="float: right; margin-right: 10px;margin-top: 10px;" @click="show4 = !show4">收起/展开</el-button>
                <el-button type='danger' style="float: right; margin-right: 10px;margin-top: 10px;" @click="deleteYangLiLabels">删除标签组</el-button>
              
              <div style="height: auto;" >
                <el-collapse-transition>
                  <div v-show="show4">
                <div v-for="JXEmotion in EmotionYangLi" :key="JXEmotion.index" >
                  <el-input   placeholder="标签名称"  style="width: 80px;" v-model="JXEmotion.label"></el-input>
                  <el-input   placeholder="标签描述信息" style="width: 200px;" v-model="JXEmotion.description"></el-input>
                  <el-input   placeholder="编码" style="width: 60px;" :value="JXEmotion.code"></el-input>
                  <span class="block" style="margin-top: 10px; margin-left: 10px;">
                    <span class="demonstration">颜色选择器</span>
                    <el-color-picker v-model="JXEmotion.color" style=" margin-top: 10px;margin-right: 10px;margin-left: 10px;"></el-color-picker>
                  </span>
                  <el-switch
                      v-model="JXEmotion.enable"
                      active-color="#13ce66"
                      inactive-color="#ff4949"
                      @change="changeEnable(JXEmotion.enable)">
                  </el-switch>
                  <el-button type="info" style="margin-right: 5px;">是否启用</el-button>
                  <el-button type="danger" @click="deleteLabelType(JXEmotion.labelType,JXEmotion.label,JXEmotion.id)">删除标签</el-button>
                </div>
                <el-button type="info" style="margin-top: 10px; margin-bottom: 10px; " @click=" addYangLiLabelType(EmotionYangLi.label)">添加标签</el-button>
                </div>
                </el-collapse-transition>
              </div> 
              </div>

              <div class="addlabel1" style="margint-top: 10px;" v-show="showaddlabel1">    
                <input type="text" value="新增标签名称" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px" v-model="addlabelname1">
                <input type="text" value="标签组描述信息" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-button type='success' style="float: right; margin-right: 10px;margin-top: 10px;" @click="show5 = !show5">收起/展开</el-button>
                <el-button type='danger' style="float: right; margin-right: 10px;margin-top: 10px;" @click="deleteaddlabel1">删除标签组</el-button>
              


              <div style="height: auto;" >
                <el-collapse-transition>
                  <div v-show="show5">
                    <div v-for="JXEmotion in EmotionAdd1" :key="JXEmotion.index" >
                  <el-input   placeholder="标签名称"  style="width: 80px;" v-model="JXEmotion.label"></el-input>
                  <el-input   placeholder="标签描述信息" style="width: 200px;"></el-input>
                  <el-input   placeholder="编码" style="width: 60px;" :value="JXEmotion.code"></el-input>
                  <span class="block" style="margin-top: 10px; margin-left: 10px;">
                    <span class="demonstration">颜色选择器</span>
                    <el-color-picker v-model="JXEmotion.color" style=" margin-top: 10px;margin-right: 10px;margin-left: 10px;"></el-color-picker>
                  </span>
                  <el-switch
                      v-model="JXEmotion.enable"
                      active-color="#13ce66"
                      inactive-color="#ff4949"
                      @change="changeEnable(JXEmotion.enable)">
                  </el-switch>
                  <el-button type="info" style="margin-right: 5px;">是否启用</el-button>
                  <el-button type="danger" @click="deleteLabelType(JXEmotion.labelType,JXEmotion.label,JXEmotion.id)">删除标签</el-button>
                </div>
                <el-button type="info" style="margin-top: 10px; margin-bottom: 10px; " @click="addEmotionAdd()">添加标签</el-button>
                </div>
                </el-collapse-transition>
              </div> 
              </div>



            <div class="addlabel2" style="margint-top: 10px;" v-show="showaddlabel2">    
                <input type="text" value="样例标签" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px" >
                <input type="text" value="标签组描述信息" style="margin-bottom: 1px; align-items: center; border: 1px dashed #ccc; padding: 10px; border-radius: 4px">
                <el-button type='success' style="float: right; margin-right: 10px;margin-top: 10px;" @click="show4 = !show4">收起/展开</el-button>
                <el-button type='danger' style="float: right; margin-right: 10px;margin-top: 10px;" @click="deleteYangLiLabels">删除标签组</el-button>
              
              <div style="height: auto;" >
                <el-collapse-transition>
                  <div v-show="show6">
                <div v-for="JXEmotion in EmotionYangLi" :key="JXEmotion.index" >
                  <el-input   placeholder="标签名称"  style="width: 80px;" v-model="JXEmotion.label"></el-input>
                  <el-input   placeholder="标签描述信息" style="width: 200px;"></el-input>
                  <el-input   placeholder="编码" style="width: 60px;" :value="JXEmotion.code"></el-input>
                  <span class="block" style="margin-top: 10px; margin-left: 10px;">
                    <span class="demonstration">颜色选择器</span>
                    <el-color-picker v-model="JXEmotion.color" style=" margin-top: 10px;margin-right: 10px;margin-left: 10px;"></el-color-picker>
                  </span>
                  <el-switch
                      v-model="JXEmotion.enable"
                      active-color="#13ce66"
                      inactive-color="#ff4949"
                      @change="changeEnable(JXEmotion.enable)">
                  </el-switch>
                  <el-button type="info" style="margin-right: 5px;">是否启用</el-button>
                  <el-button type="danger" @click="deleteLabelType(JXEmotion.labelType,JXEmotion.label,JXEmotion.id)">删除标签</el-button>
                </div>
                <el-button type="info" style="margin-top: 10px; margin-bottom: 10px; " @click=" addYangLiLabelType(EmotionYangLi.label)">添加标签</el-button>
                </div>
                </el-collapse-transition>
              </div> 
              </div>




            </div>

            <div slot="footer" class="dialog-footer">
              
              <el-button  @click="undoAlterLabel">取 消</el-button>
              <el-button type="primary" @click="confirmAlterLabel">确 定</el-button>
            </div>
          </el-dialog>






        </div>

        <!-- 播放器，最多四个 -->
        <div class="video-players-container">
          <div v-show="showPlayer1" class="video-player1">
            <video-player 
              class="video-player vjs-custom-skin"
              ref="videoPlayer1"
              :playsinline="true"
              :options="playerOptions[0]"
              @timeupdate="timeUpdate(1)"
              @loadeddata="set(1)"
            ></video-player>
            <div class="canvas-container1">
              <canvas id="canvas1"></canvas>
            </div>
            <div class="canvas-container1-label">
              <canvas id="labelCanvas1"></canvas>
            </div>
          </div>

          <div v-show="showPlayer2" class="video-player2">
            <video-player 
              class="video-player vjs-custom-skin"
              ref="videoPlayer2"
              :playsinline="true"
              :options="playerOptions[1]"
              @timeupdate="timeUpdate(2)"
              @loadeddata="set(2)"
            ></video-player>
            <div class="canvas-container2">
              <canvas id="canvas2"></canvas>
            </div>
            <div class="canvas-container2-label">
              <canvas id="labelCanvas2"></canvas>
            </div>
          </div>

          <div v-show="showPlayer3" class="video-player3">
            <video-player 
              class="video-player vjs-custom-skin"
              ref="videoPlayer3"
              :playsinline="true"
              :options="playerOptions[2]"
              @timeupdate="timeUpdate(3)"
              @loadeddata="set(3)"
            ></video-player>
            <div class="canvas-container3">
              <canvas id="canvas3"></canvas>
            </div>
            <div class="canvas-container3-label">
              <canvas id="labelCanvas3"></canvas>
            </div>
          </div>

          <div v-show="showPlayer4" class="video-player4">
            <video-player 
              class="video-player vjs-custom-skin"
              ref="videoPlayer4"
              :playsinline="true"
              :options="playerOptions[3]"
              @timeupdate="timeUpdate(4)"
              @loadeddata="set(4)"
            ></video-player>
            <div class="canvas-container4">
              <canvas id="canvas4"></canvas>
            </div>
            <div class="canvas-container4-label">
              <canvas id="labelCanvas4"></canvas>
            </div>
          </div>

          <div class="canvas-container-text">
            <canvas id="canvas-text"></canvas>
          </div>
          <!-- <div class="canvas-container-label">
            <canvas id="canvas-label"></canvas>
          </div> -->
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
        <div style="position: absolute; bottom: 90px; left: 48px;">
          <el-button v-for="item in this.buttonColors[this.finalSelectedSingleLabelType]" :key="item" @click="annotation(item.label)" :style="{ backgroundColor: item.color }">{{ item.label }}</el-button>
          <el-button v-show="this.finalSelectedSingleLabelType" @click="cancelAnnotation()">取消</el-button>
        </div>
        <div style="position: absolute; bottom: 50px; left: 48px;">
          <el-button v-for="item in this.buttonColors[this.finalSelectedMultiLabelType]" :key="item" @click="annotation(item.label)" :style="{ backgroundColor: item.color }">{{ item.label }}</el-button>
          <el-button v-show="this.finalSelectedMultiLabelType" @click="cancelAnnotation()">取消</el-button>
        </div>
      </el-main>

      <!-- 展示部分 -->
      <el-main style="box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); width: 34%">
        <!-- 导出 -->
        <el-button type="primary" plain @click="exportRecordAndText" style="margin-bottom: 10px;">导出</el-button>
        
        <!-- 表格 -->
        <el-table v-if="showRecordTable" :data="recordData" height="580" :default-sort = "{prop: 'start', order: 'ascending'}" border :highlight-current-row="true" style="width: 100%;">
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
                <el-option v-for="label in buttonColors[selectedPersonId === '全选'?finalSelectedMultiLabelType:finalSelectedSingleLabelType].map((item) => item.label)" :key="label" :label="label" :value="label"></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column min-width="24%" label="修改">
            <template slot-scope="scope">
              <el-button size="mini" type="text" class="is-text1" @click="updateRecord(scope.$index, scope.row)">修改</el-button>
              <el-popconfirm
                  title="您确定删除吗？"
                  @confirm="deleteRecord(scope.row)"
              >
                <el-button style="margin-left: 20px;" slot="reference" size="mini" type="text" class="is-text2">删除</el-button>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-else :data="textData" height="580" :default-sort = "{prop: 'start', order: 'ascending'}" border :highlight-current-row="true" style="width: 100%;">
          <el-table-column fixed="left" width="100px" label="操作">
            <template slot-scope="scope">
              <el-button size="mini" type="text" class="is-text1" @click="updateText(scope.$index, scope.row)">修改</el-button>
              <el-button size="mini" type="text" class="is-text2" @click="deleteText(scope.row)">删除</el-button>
            </template>
          </el-table-column>
          <el-table-column width="200px" prop="text" label="text">
            <template slot-scope="scope">
              <span v-if="isEditingTextRow !== scope.$index">
                <el-input type="textarea" :disabled="true" :rows="1" v-model="scope.row.text" size="mini">
                </el-input>
              </span>
              <el-input v-else type="textarea" :rows="1" v-model="scope.row.text" size="mini">
              </el-input>
            </template>
          </el-table-column>
          <el-table-column width="100px" prop="start" label="开始" sortable>
            <template slot-scope="scope">
              <span v-if="isEditingTextRow !== scope.$index"
                @click="jumpToTime(scope.row.start)"
                style="cursor: pointer">{{ scope.row.start }}</span>
              <el-input v-else v-model="scope.row.start" size="mini"></el-input>
            </template>
          </el-table-column>
          <el-table-column width="100px" prop="end" label="结束" sortable>
            <template slot-scope="scope">
              <span v-if="isEditingTextRow !== scope.$index"
                @click="jumpToTime(scope.row.end)"
                style="cursor: pointer">{{ scope.row.end }}</span>
              <el-input v-else v-model="scope.row.end" size="mini"></el-input>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  data() {
    return {
      show1:true,//更改标签页面中 基本情绪标签的展开折叠
      show2:true,//更改标签页面中 情绪标签的展开折叠
      show3:true,//更改标签页面中 极性标签的展开折叠
      show4:true,//更改标签页面中 样例标签的展开折叠
      allPersonEmotions:[],//所有的个人标签
      allGroupEmotions:[],//所有的小组标签
      personEmotionsTotol:[],//个人标签类型的总数
      showQingXvLabels:true,//是否展示情绪标签
      showJiXingLabels:true,//是否展示极性标签
      showYangLiLabels:true,//是否展示样例标签
      groupEmotionsTotol:[],//小组标签类型的总数
      baseEmotion:[],//此数组声明为基本情绪数组标签
      EmotionJiXing:[],//此数组声明为极性数组标签
      EmotionQingXv:[],//此数组声明为情绪数组标签
      EmotionYangLi:[],//此数组声明为样例数组标签
      showBasicLabel:true,//第一个标签删除后 该变量为flase，表示不展示
      allEmotions:[],//此数组声明为全部标签数组
      startEmotions:[],//更改标签前的初始标签内容
      finalEmotions:[],//最终选择的标签数组
      EmotionName1:"",//第一个标签的名称
      EmotionName2:"",//第二个标签的名称
      EmotionName3:"",//第二个标签的名称
      EmotionName4:"",//第二个标签的名称
      EmotionName5:"",//第二个标签的名称
      EmotionName6:"",//第二个标签的名称
      showaddlabel1:false,//第一个添加标签的弹窗
      showaddlabel2:false,//第二个添加标签的弹窗
      show5:true,//第一个标签隐藏后 该变量为flase，表示不展示
      show6:true,//第二个标签隐藏后 该变量为flase，表示不展示
      EmotionAdd1:[],//第一个标签的添加标签数组
      EmotionAdd2:[],//第二个标签的添加标签数组
      EmotionAdd3:[],//第三个标签的添加标签数组
      showaddd1:true,//第一个标签添加标签的弹窗
      addlabelname1:'',//第一个标签的添加标签名称
      addlabelname2:"",//第一个标签的添加标签名称
      addlabelname3:"",//第一个标签的添加标签名称
      //////////////////////////登录、用户信息//////////////////////////
      user: JSON.parse(localStorage.getItem('user') || '{}'),  // 获取当前登录的user信息
      usernames: [],  // 存放username
      color1:'#409EFF',//颜色选择框的默认值
      qiyongvalue:true, //是否启用,
      showSelectAnnotator:false,  // 是否展示标注者下拉框
      selectedUsername: null,  // 选择标注者
      firstSelectedUsername: null,  // 第一次选择的标注者
      //////////////////////////选择视频并播放//////////////////////////
      isVideoDisabled: false,
      videoDrawer: false,  // 是否打开选择视频drawer
      treeData: [],  // 这里存放构建好的树形数据
      treeProps: {
        label: 'label',  // 每个节点显示的文本
        children: 'children'  // 子节点数组的字段名
      },
      selectedVideos: null,  // 当前确定选中的视频节点
      videoPaths: [],  // 视频路径数组
      videoNames: [],  // 视频名称数组
      width: 0,  // 每个播放器的宽度
      height: 0,  // 每个播放器的高度
      //////////////////////////选择标签组//////////////////////////
      labelTypeDialog: false,  // 是否打开选择标签的dialog
      alterLabelTypeDialog: false,  // 是否打开更改标签的dialog
      selectedSingleLabelType: null,  // 单个标签
      selectedMultiLabelType: null,  // 小组标签
      finalSelectedSingleLabelType: null,  // 最终选择
      finalSelectedMultiLabelType: null,
      singleLabelTypes: [],  // 单个选择
      multiLabelTypes: [],  // 小组选择
      labelTypeMap: [],  // labelType数组
      buttonColors: [],  // 按钮颜色
      //////////////////////////播放器下拉框//////////////////////////
      selectedVideoIndex: 0,  // 播放器下标
      showPlayer1: true,  // 是否展示该播放器
      showPlayer2: true,
      showPlayer3: true,
      showPlayer4: true,
      lastIndex: 0,  // 上次选择的播放器下标
      //////////////////////////播放器和进度条设置//////////////////////////
      duration: null,  // 总时长
      currentTime: null,  // 当前时间
      progress: 0,  // 当前进度
      maxProgress: 0,  // 最大进度
      showMark: false,  // 是否展示黑线
      translateXValue: 0,  // 偏移量
      recordSegments: [],  // 存储Record数组
      isPlaying: false,  // 是否播放
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
            playToggle: false,            // 暂停和播放键
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
            playToggle: false,            // 暂停和播放键
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
            playToggle: false,            // 暂停和播放键
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
            playToggle: false,            // 暂停和播放键
            progressControl: false        // 进度条
          }
        }
      ],  // 视频播放器设置
      //////////////////////////被标注者下拉框//////////////////////////
      personIds: [],  // 该小组所有person_id
      personIdNameMap: {}, // person_id和名字的映射
      colorMap: {},  // 矩形框颜色
      colors: ['blue', 'red', 'green', 'yellow', 'orange', 'black'],  // 所有矩形框的可取颜色
      selectedPersonId: null,  // 选择personId
      //////////////////////////打标签和数据表格展示//////////////////////////
      showRecordTable: true,  // 是否展示record数据
      recordData: [],  // record数据
      isEditingRow: null,  // 正在编辑的行
      annotationInProgress: null,  // 用于存储当前正在标注的数据
      currentAnnotationLabel: null,  // 用于存储当前标注的标签
      firstStart: null,  // 初始的开始时间
      firstEnd: null,  // 初始的结束时间
      firstLabel: null,  // 初始的标签
      labelCanvas: {},  // 画布
      //////////////////////////矩形框下拉框//////////////////////////
      selectedRect: null,  // 用于查询Rect
      //////////////////////////画布和画框//////////////////////////
      canvas: {},  // 画布
      startDrawMode: false,  // 开启画框模式
      isDrawing: false,  // 正在画框
      startPoint: null,  // 鼠标起始点
      endPoint: null,  // 鼠标结束点
      rects: {},  // 保存每个播放器以及person_id对应的矩形框，用于发送数据到数据库，三维对象
      faces: {},  // 用于从数据库中获取每一条face数据
      facesCache: {}, // 本地缓存
      //////////////////////////文本下拉框//////////////////////////
      selectedText: null,  // 选择某个personId对应的文本
      //////////////////////////文本和字幕//////////////////////////
      canvasText: null,  // 字幕画布
      textData: [],  // text数据
      textSegments: [],  // text数组，用于彩带显示
      isEditingTextRow: null,  // 正在编辑的行
      annotationTextInProgress: null,  // 用于存储当前正在标注的数据
      firstTextStart: null,  // 初始的开始时间
      firstTextEnd: null,  // 初始的结束时间
      firstText: null,  // 初始的文本
      videoMember: [],  // 读取的member
      // ip: "10.131.102.150",
      ip: "localhost",


  
    }

  
  },
  async created() {
    // 加载视频和标签组和username
    await this.loadVideo()
    await this.loadLabelType()
    await this.loadUsername()

    // 如果是管理员则能够查看所有标注者
    if (this.user.role === "管理员") {
      this.showSelectAnnotator = true
      this.selectedUsername = this.user.username
      this.firstSelectedUsername = this.user.username
    }
  },
  mounted() {
    // 以第一个播放器的宽高作为基准
    this.width = this.$refs.videoPlayer1.$el.offsetWidth
    this.height = this.$refs.videoPlayer1.$el.offsetHeight

    // 监听画框操作
    document.addEventListener('keydown', this.drawKeyDown)  // 画框
    document.addEventListener('keydown', this.textKeyDown)  // 标记文本
  },
  methods: {
    deleteQingXvLabels(){
      this.EmotionQingXv=[]
      this.showQingXvLabels=false
    },
    deleteJiXingLabels(){
      this.EmotionJiXing=[]
      this.showJiXingLabels=false
    },
    deleteYangLiLabels(){
      this.EmotionYangLi=[]
      this.showYangLiLabels=false
    },
    AlterName1(){
      console.log(this.inputValue)
    },
    changeEnable(enable){
      enable=!enable
    },
    //////////删除第一个新增的个人标签//////
    deleteaddlabel1(){
      this.showaddlabel1=false
      this.EmotionAdd1=[]
    },
    //////////删除第二个新增的个人标签//////
    deleteaddlabel1(){
      this.showaddlabel2=false
      this.EmotionAdd2=[]
    },
    addEmotionAdd(){
      this.EmotionAdd1.push({label:" ",color:"#409EFF",code:"",enable:true,isGroup:0,labelType:this.addlabelname1})
    },
    //////////删除第三个新增的个人标签//////
    /////////新增个人标签////////////
    addPersonalLabelType(){
      if(this.showaddlabel1 === false){
        this.showaddlabel1 = true
        this.EmotionAdd1.push({label:" ",color:"#409EFF",code:"",enable:true,isGroup:0,labelType:this.addlabelname1})
      }
      else if(this.showaddlabel2 === false){
        this.showaddlabel2 = true
      }
    },
    ////////////删除标签组///////////
    /////////////删除情绪组标签/////////////////
    deleteBasicLabel(){
      this.showBasicLabel=false
      this.baseEmotion=[]
    },
    addQingXvLabelType(labelname){
      this.EmotionQingXv.push({label:labelname,color:"#409EFF"})
    },
    addJiXingLabelType(labelname){
      this.EmotionJiXing.push({label:labelname,color:"#409EFF"})
    },
    addYangLiLabelType(labelname){
      this.EmotionYangLi.push({label:labelname,color:"#409EFF"})
    },
    ////////添加基本情绪标签/////////
    addBaseLabelType(labelname){
      this.baseEmotion.push({label:labelname,color:"#409EFF"})
      console.log(this.baseEmotion)
    },
    undoAlterLabel(){
      this.finalEmotions=this.startEmotions.slice(0)
      console.log(this.finalEmotions)
    },
     async confirmAlterLabel(){
      this.alterLabelTypeDialog=!this.alterLabelTypeDialog
      this.finalEmotions=this.baseEmotion.concat(this.EmotionJiXing,this.EmotionQingXv,this.EmotionYangLi,this.EmotionAdd1,this.EmotionAdd2,this.EmotionAdd3)
      console.log(this.finalEmotions)
      const res =await request.post("/label/updateAllLabels",this.finalEmotions)
    },
    // updateEmotion(){
    //   this.baseEmotion= this.allEmotions.filter(item => item.labelType === "基本情绪")
    //   this.EmotionJiXing = this.allEmotions.filter(item => item.labelType === "情绪极性")
    //   this.EmotionQingXv = this.allEmotions.filter(item => item.labelType === "情绪")
    //   this.EmotionYangLi = this.allEmotions.filter(item => item.labelType === "样例")
    // },
    /////////////////删除某个情绪标签/////////
    deleteLabel(labeltypenames,labelname,id){
      //this.baseEmotion=this.baseEmotion.filter(item=>item.label!==labelname)
      // this.EmotionJiXing=this.EmotionJiXing.filter(item=>item.label!==labelname)
      // this.EmotionQingXv=this.EmotionQingXv.filter(item=>item.label!==labelname)
      // this.EmotionYangLi=this.EmotionYangLi.filter(item=>item.label!==labelname)
      for(let i=0;i<this.allEmotions.length;i++){
        if(this.allEmotions[i].labelType===labeltypenames && this.allEmotions[i].label===labelname&&this.allEmotions[i].id===id){
          this.allEmotions.splice(i,1)
          console.log("删除一个")
          break
        }
      }
      this.baseEmotion= this.allEmotions.filter(item => item.labelType === "基本情绪")
      this.EmotionJiXing = this.allEmotions.filter(item => item.labelType === "情绪极性")
      this.EmotionQingXv = this.allEmotions.filter(item => item.labelType === "情绪")
      this.EmotionYangLi = this.allEmotions.filter(item => item.labelType === "样例")
      console.log(this.baseEmotion)
    }, 
    updateEmotions(){
      this.baseEmotion= this.allEmotions.filter(item => item.labelType === "基本情绪")
      this.EmotionJiXing = this.allEmotions.filter(item => item.labelType === "情绪极性")
      this.EmotionQingXv = this.allEmotions.filter(item => item.labelType === "情绪")
      this.EmotionYangLi = this.allEmotions.filter(item => item.labelType === "样例")
      console.log(this.baseEmotion)
    },
  
    ///////////////// 选择标注者 /////////////////
    // 加载所有user姓名
    async loadUsername() {
      // 获取所有username
      const res = await this.$request.get("/user/getUsernames")
      this.usernames = res.data
    },
    deleteBasicLabel(){
        {
        this.$confirm('此操作将删除该标签, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.baseEmotion=[]
          this.showBasicLabel=false
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });          
        });
      }
    },
    deleteQingXvLabels(){
        {
        this.$confirm('此操作将删除该标签, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.EmotionQingXv=[]
          this.showQingXvLabels=false
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });          
        });
      }
    },
    deleteJiXingLabels(){
        {
        this.$confirm('此操作将删除该标签, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.EmotionJiXing=[]
          this.showJiXingLabels=false
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });          
        });
      }
    },   
    deleteYangLiLabels(){
        {
        this.$confirm('此操作将删除该标签, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.EmotionYangLi=[]
          this.showYangLiLabels=false
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });          
        });
      }
    },      
    /////////////////////删除标签需要弹出 确认框 //////////////////
    deleteLabelType(labeltypenames,labelname,id){
        {
        this.$confirm('此操作将删除该标签, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          console.log(labeltypenames,labelname)
          this.deleteLabel(labeltypenames,labelname,id);
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });          
        });
      }
    
    },
    // 筛选标注者
    showRecordAndRectByUid() { 
      if (this.selectedVideos === null) {
        this.$message.error("请先选择视频")
        this.selectedUsername = this.firstSelectedUsername
      } else if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.annotationInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.annotationTextInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.$request.get("/user/getIdByUsername", {
          params: {
            username: this.selectedUsername
          }
        }).then(res => {
          // 将当前管理员的id切换为选择的那个username的id
          this.user.id = res.data

          // 再根据切换了的id来展示record和rect和Text
          this.showRecord()
          this.showRect()
          this.showText()
        })
      }
    },

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
      if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.selectedVideoIndex !== 0) {
        this.$message.error("请切换到全选再选择视频")
      } else if (this.annotationInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.annotationTextInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingTextRow !== null) {
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
      }))

      // 检查所选视频是否来自同一小组
      const sameGroup = this.selectedVideos.every(video => video.parent[1].label === this.selectedVideos[0].parent[1].label)

      // 如果选中节点长度为0
      if (this.selectedVideos.length === 0) {
        this.$message.error("还未选择视频")
        return
      }

      if (!sameGroup) {
        this.$message.error("请选择同一小组的视频")
        return
      }

      this.videoDrawer = false

      // 清空当前选择
      this.videoPaths = []
      this.videoNames = []

      // 将视频信息存入数组
      for(const node of this.selectedVideos) {
        this.videoPaths.push(node.videoPath)
        this.videoNames.push(node.videoName)
      }

      // 在播放视频前将当前用不到的播放器的src置空
      for (let i = this.selectedVideos.length; i < this.playerOptions.length; i++) {
        this.playerOptions[i].sources[0].src = ''
      }
      
      // 清空一些选择
      this.finalSelectedSingleLabelType = null
      this.finalSelectedMultiLabelType = null
      this.selectedPersonId = null
      this.selectedRect = null
      this.selectedText = null
      this.recordData = []
      this.recordSegments = []
      this.textData = []
      this.textSegments = []

      // 播放视频、展示标签
      await this.showVideo()
      // 加载学生id和姓名
      await this.loadPersonId()
      this.loadName()
    },

    // 播放视频
    async showVideo() {
      // 循环播放
      for (let i = 0; i < this.selectedVideos.length; i++) {
        this.playerOptions[i].sources[0].src = "http://" + this.ip + ":8888/video/showVideo?videoPath=" + this.videoPaths[i] + "&" + "videoName=" + this.videoNames[i];
      }
    },

    ///////////////// 选择标签组 /////////////////
    // 加载标签
    async loadLabelType() {
      // 获取所有label数据
      const res = await this.$request.get("/label/getAllLabel")

      // 过滤出个人标注和小组标注的标签
      this.singleLabelTypes = [...new Set(res.data.filter(label => label.isGroup === 0).map(label => label.labelType))]
      this.multiLabelTypes = [...new Set(res.data.filter(label => label.isGroup === 1).map(label => label.labelType))]
    
      // 给buttonColors赋初值
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
      } else if (this.annotationTextInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.labelTypeDialog = true
      }
    },
    async getAlterLabelType(){
      this.alterLabelTypeDialog = true//此语句为点击 更改标签按钮显示新的页面
      this.labelTypeDialog = false////此语句为点击 更改标签按钮   不显示之前的页面
      const res = await this.$request.get("/label/getAllLabel")//从数据库中的到所有的标签
      this.allEmotions=res.data//alEmotions为所有标签数组
      this.startEmotions=this.allEmotions.slice()
      console.log(this.allEmotions);
      const allPersonEmotions=this.allEmotions.filter(item => item.isGroup === 0)
      const allGroupEmotions=this.allEmotions.filter(item => item.isGroup === 1)
      const personEmotionsTotol=Array.from(new Set(allPersonEmotions.map(item => item.labelType)))
      const groupEmotionsTotol=Array.from(new Set(allGroupEmotions.map(item => item.labelType)))
      this.baseEmotion= this.allEmotions.filter(item => item.labelType === "基本情绪")
      this.EmotionJiXing = this.allEmotions.filter(item => item.labelType === "情绪极性")
      this.EmotionQingXv = this.allEmotions.filter(item => item.labelType === "情绪")
      this.EmotionYangLi = this.allEmotions.filter(item => item.labelType === "样例")
      // console.log(this.baseEmotion[0]["labelType"])
      // this.get1EmotionType(this.baseEmotion[0])
      // this.get2EmotionType(this.EmotionJiXing[0])
      // this.get3EmotionType(this.EmotionQingXv[0])
      // this.get4EmotionType(this.EmotionYangLi[0])
    },
    // get1EmotionType(label){
    //   this.EmotionName1=label["labelType"]
    // },
    // get2EmotionType(label){
    //   this.EmotionName2=label["labelType"]
    // },
    // get3EmotionType(label){
    //   this.EmotionName3=label["labelType"]
    // },
    // get4EmotionType(label){
    //   this.EmotionName4=label["labelType"]
    // },
    // 清空标签组
    clearLabelType() {
      this.selectedSingleLabelType = null
      this.finalSelectedSingleLabelType = null
      this.selectedMultiLabelType = null
      this.finalSelectedMultiLabelType = null
    },

    // 关闭标签dialog
    closeLabelType() {
      this.selectedSingleLabelType = this.finalSelectedSingleLabelType
      this.selectedMultiLabelType = this.finalSelectedMultiLabelType
      this.labelTypeDialog = false
    },
    closeAlterLabelType(){
      //待写其他代码
      this.alterLabelTypeDialog = false
    },
    //更改标签组
    alterLabelType() {
      console.log("更改标签组")
    },

    // 选择标签组
    chooseLabelType() {
      this.finalSelectedSingleLabelType = this.selectedSingleLabelType
      this.finalSelectedMultiLabelType = this.selectedMultiLabelType
      this.labelTypeDialog = false
      this.showRecord()  // 展示record数据
    },

    ///////////////// 播放器和进度条设置 /////////////////
    // 选择播放器
    async chooseVideoPlayer(videoIndex) {
      if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else if (this.annotationInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.annotationTextInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        if (videoIndex !== 0) {  // 单个视频
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
            this.labelCanvas[videoIndex].setDimensions({
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
            // 如果上一次选的是单个视频，此次要先将上次选的视频画布缩小
            this.labelCanvas[this.lastIndex].setDimensions({
              width: this.width,
              height: this.height
            })
            // 再放大当前画布
            this.labelCanvas[videoIndex].setDimensions({
              width: this.width * 2,
              height: this.height * 2
            })
          }
          
          // 设置画布穿透
          document.querySelector('.canvas-container1').style.pointerEvents = videoIndex === 1 ? 'none' : 'auto'
          document.querySelector('.canvas-container2').style.pointerEvents = videoIndex === 2 ? 'none' : 'auto'
          document.querySelector('.canvas-container3').style.pointerEvents = videoIndex === 3 ? 'none' : 'auto'
          document.querySelector('.canvas-container4').style.pointerEvents = videoIndex === 4 ? 'none' : 'auto'

          // 清除画布上的所有对象
          this.canvas[videoIndex].getObjects().forEach(obj => {
            this.canvas[videoIndex].remove(obj)
          })
          this.canvasText.getObjects().forEach(obj => {
            this.canvasText.remove(obj)
          })
          this.labelCanvas[videoIndex].getObjects().forEach(obj => {
            this.labelCanvas[videoIndex].remove(obj)
          })

          // 记录上一次最后选择的播放器索引
          this.lastIndex = videoIndex
          this.showRecord()
          this.showRect()
          this.showText()
          this.showPersonLabel()
        } else {  // 多个视频
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
          this.labelCanvas[this.lastIndex].setDimensions({
            width: this.width,
            height: this.height
          })

          // 重置所有画布穿透
          document.querySelector('.canvas-container1').style.pointerEvents = 'none'
          document.querySelector('.canvas-container2').style.pointerEvents = 'none'
          document.querySelector('.canvas-container3').style.pointerEvents = 'none'
          document.querySelector('.canvas-container4').style.pointerEvents = 'none'
          
          // 清除画布上的所有对象
          for (let i = 1; i <= this.selectedVideos.length; i++) {
            this.canvas[i].getObjects().forEach(obj => {
              this.canvas[i].remove(obj)
            })
          }
          this.canvasText.getObjects().forEach(obj => {
            this.canvasText.remove(obj)
          })
          for (let i = 1; i <= this.selectedVideos.length; i++) {
            this.labelCanvas[i].getObjects().forEach(obj => {
              this.labelCanvas[i].remove(obj)
            })
          }

          // 记录上一次最后选择的播放器索引
          this.lastIndex = videoIndex
          this.showRecord()
          this.showRect()
          this.showText()
          this.showPersonLabel()
        }
      }
    },

    // 加载person_id数据
    async loadPersonId() {
      // 清空上次加载的personIds
      this.personIds = []
      // 获取对应小组group_id的所有成员person_id
      const res = await this.$request.get("/group/getPersonIds", {
        params: {
          groupId: this.selectedVideos[0].parent[1].id,
        }
      })
      // 给this.personIds赋值
      this.personIds = res.data.split(',').map(item => parseInt(item.trim()))
    },

    // 加载姓名
    loadName() {
      // 清空上次加载的personIdNameMap以及colorMap
      this.personIdNameMap = {}
      this.colorMap = {}

      // 循环初始化
      for (const personId of this.personIds) {
        const index = this.personIds.indexOf(personId)
        this.$request.get("/person/getPerson", {
          params: {
            id: personId
          }
        }).then(res => {
          // person_id与name的映射
          this.personIdNameMap[personId] = res.data[0].number + '_' + res.data[0].name
        })
        // 给矩形框添加颜色
        this.colorMap[personId] = this.colors[index]
      }
    },

    // 加载对应小组的所有视频对应的member
    loadVideoMember() {
      // 清空上次加载的videoMember
      this.videoMember = []
      this.$request.get("/video/getMember", {
        params: {
          classId: this.selectedVideos[0].parent[0].id,
          groupId: this.selectedVideos[0].parent[1].id,
        }
      }).then(res => {
        this.videoMember = res.data
      })
    },

    // 初始化
    set(videoIndex){
      if (this.playerOptions[videoIndex-1].sources[0].src !== '') {
        this.duration = this.$refs[`videoPlayer${videoIndex}`].player.duration()
        this.maxProgress = Math.floor(this.duration)

        if (!this.canvas[videoIndex]) {
          // 初始化画布
          this.canvas[videoIndex] = new fabric.Canvas(`canvas${videoIndex}`, {
            backgroundColor: 'rgba(0, 0, 0, 0)',
            width: this.width,
            height: this.height
          })
        }
        
        if (!this.canvasText) {
          this.canvasText = new fabric.Canvas(`canvas-text`, {
            backgroundColor: 'rgba(0, 0, 0, 0)',
            width: this.width * 2,
            height: this.height * 2
          })
        }

        if (!this.labelCanvas[videoIndex]) {
          // 初始化画布
          this.labelCanvas[videoIndex] = new fabric.Canvas(`labelCanvas${videoIndex}`, {
            backgroundColor: 'rgba(0, 0, 0, 0)',
            width: this.width,
            height: this.height
          })
        }

        // 为矩形框this.rects三维对象赋值
        this.rects[videoIndex] = {}
        this.personIds.forEach(personId => {
          this.rects[videoIndex][personId] = {}  // 初始化this.rects
        })

        // 监听键盘
        document.addEventListener('keydown', this.videoKeyDown)
      }
    },

    // 监听键盘
    videoKeyDown(event) {
      if (this.showMark) {
        this.$message.error("请先完成打标签操作")
      } else {
        const videoIndex = this.selectedVideoIndex
        if (videoIndex === 0) {
          try {
            switch (event.key) {
              case ' ':  // 暂停或播放
                if (this.isEditingTextRow !== null) {
                  this.$message.error("请先完成修改")
                } else if (this.isEditingRow !== null) {
                  this.$message.error("请先完成修改")
                } else {
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
                break
              case 'z':  // 快退5秒
                for (let i = 0; i < this.selectedVideos.length; i++) {
                  this.$refs[`videoPlayer${i+1}`].player.currentTime(this.currentTime - 5)
                }
                break
              case 'x':  // 快退1秒
                for (let i = 0; i < this.selectedVideos.length; i++) {
                  this.$refs[`videoPlayer${i+1}`].player.currentTime(this.currentTime - 1)
                }
                break
              case 'c':  // 快进1秒
                for (let i = 0; i < this.selectedVideos.length; i++) {
                  this.$refs[`videoPlayer${i+1}`].player.currentTime(this.currentTime + 1)
                }
                break
              case 'v':  // 快进5秒
                for (let i = 0; i < this.selectedVideos.length; i++) {
                  this.$refs[`videoPlayer${i+1}`].player.currentTime(this.currentTime + 5)
                }
                break
            }
          } catch (error) {
            console.log(error)
          }
        } else {
          try {
            switch (event.key) {
              case ' ':  // 暂停或播放
                if (this.isEditingTextRow !== null) {
                  this.$message.error("请先完成修改")
                } else if (this.isEditingRow !== null) {
                  this.$message.error("请先完成修改")
                } else {
                  if (this.$refs[`videoPlayer${videoIndex}`].player.paused() === false) {
                    this.$refs[`videoPlayer${videoIndex}`].player.pause()
                  } else {
                    this.$refs[`videoPlayer${videoIndex}`].player.play()
                  }
                }
                break
              case 'z':  // 快退10秒
                this.$refs[`videoPlayer${videoIndex}`].player.currentTime(this.currentTime - 10)
                break
              case 'x':  // 快退5秒
                this.$refs[`videoPlayer${videoIndex}`].player.currentTime(this.currentTime - 5)
                break
              case 'c':  // 快进5秒
                this.$refs[`videoPlayer${videoIndex}`].player.currentTime(this.currentTime + 5)
                break
              case 'v':  // 快进10秒
                this.$refs[`videoPlayer${videoIndex}`].player.currentTime(this.currentTime + 10)
                break
            }
          } catch (error) {
            console.log(error)
          }
        }
      }
      
    },

    // 播放器时间触发函数
    timeUpdate(videoIndex) {
      // 如果当前播放器没有播放内容则返回
      if (this.playerOptions[videoIndex-1].sources[0].src === '') {
        return
      }

      // 给进度条属性赋值
      this.currentTime = Math.floor(this.$refs[`videoPlayer${videoIndex}`].player.currentTime())
      this.progress = this.currentTime

      // 随着视频播放动态显示rect和text
      if (this.selectedRect !== null && this.selectedRect !== '') {
        this.showRect()
      }
      if (this.selectedText !== null && this.selectedText !== '') {
        this.showText()
      }
      if (this.selectedPersonId !== null && this.selectedPersonId !== '') {
        this.showPersonLabel()
      }
    },

    // 进度条触发函数
    sliderChange() {
      // 当前时间
      this.currentTime = this.progress

      if (this.selectedVideoIndex === 0) {  // 全选
        for (let i = 0; i < this.selectedVideos.length; i++) {
          this.$refs[`videoPlayer${i+1}`].player.currentTime(this.progress)
        }
      } else {  // 单个
        // 选择单个视频则只需要将视频当前时间置为进度条的进度即可
        this.$refs[`videoPlayer${this.selectedVideoIndex}`].player.currentTime(this.progress)
      }

      // 随着视频播放动态显示rect和text
      if (this.selectedRect !== null) {
        this.showRect()
      }
      if (this.selectedText !== null) {
        this.showText()
      }
      if (this.selectedPersonId !== null && this.selectedPersonId !== '') {
        this.showPersonLabel()
      }
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
      } else if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        if (this.selectedPersonId === null) {
          return
        } else if (this.selectedPersonId === '') {
          // 如果点击clear按钮则清空数据
          this.recordData = []
          this.recordSegments = []
        } else if (this.selectedPersonId === '全选') {
          this.showRecordTable = true  // 展示record表格
          this.$request.get("/groupRecord/getGroupRecord", {
            params: {
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              uid: this.user.id,
              labelType: this.finalSelectedMultiLabelType,
            }
          }).then(res => {
            this.recordData = res.data
            this.createRecordSegments(this.recordData)  // 制作彩带数组
          })
        } else {
          this.showRecordTable = true  // 展示record表格
          this.$request.get("/record/getRecord", {
            params: {
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              personId: this.selectedPersonId,
              uid: this.user.id,
              labelType: this.finalSelectedSingleLabelType
            }
          }).then(res => {
            this.recordData = res.data
            this.createRecordSegments(this.recordData)  // 制作彩带数组
          })
        }
      }
    },

    showPersonLabel() {
      if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        if (this.selectedPersonId === null) {
          //不展示
        } else if (this.selectedPersonId === '') {
          //不展示
        } else {
          let findSegment = null
          this.recordSegments.forEach(segment => {
            if (this.currentTime >= this.timeToSeconds(segment.start) && this.currentTime < this.timeToSeconds(segment.end)) {
              findSegment = segment
            }
          })
          
          if (this.selectedVideoIndex !== 0) {
            // 清除画布上的所有对象
            this.labelCanvas[this.selectedVideoIndex].getObjects().forEach(obj => {
              this.labelCanvas[this.selectedVideoIndex].remove(obj)
            })

            let Textbox = new fabric.Textbox(findSegment.label, {
              width: this.width * 2,
              fontSize: 20,
              fill: findSegment.color,
              textAlign: 'center', // 设置文本居中对齐
              splitByGrapheme: true, // 允许按字符进行换行
            })
            this.labelCanvas[this.selectedVideoIndex].add(Textbox)
            this.labelCanvas[this.selectedVideoIndex].renderAll()  // 重新渲染
          } else {  // 全选
            for (let i = 1; i <= this.selectedVideos.length; i++) {
              this.labelCanvas[i].getObjects().forEach(obj => {
                this.labelCanvas[i].remove(obj)
              })
            }
            let Textbox = new fabric.Textbox(findSegment.label, {
              width: this.width,
              fontSize: 20,
              fill: findSegment.color,
              textAlign: 'center', // 设置文本居中对齐
              splitByGrapheme: true, // 允许按字符进行换行
            })
            this.labelCanvas[this.selectedPersonId - this.personIds[0] + 1].add(Textbox)
            this.labelCanvas[this.selectedPersonId - this.personIds[0] + 1].renderAll()  // 重新渲染
          }
        }
      }
    },

    // 创建recordSegments数组用于后续显示彩带
    createRecordSegments(recordData) {
      this.recordSegments = recordData.map(recordSegment => ({
        start: recordSegment.start,  // 开始时间
        end: recordSegment.end,  // 结束时间
        label: recordSegment.label,
        color: this.buttonColors[this.selectedPersonId === '全选' ? this.finalSelectedMultiLabelType : this.finalSelectedSingleLabelType].find(item => item.label === recordSegment.label).color
      }))
    },

    // 显示彩带
    generateProcess() {
      if (this.showRecordTable === true) {
        // 如果展示的是record
        return this.recordSegments.map(recordSegment => {
          // 将数据库中的数据转化为秒数
          const startInSeconds = this.timeToSeconds(recordSegment.start)
          const endInSeconds = this.timeToSeconds(recordSegment.end)

          // 计算色块所占百分比
          const startPercentage = (startInSeconds / Math.max(this.duration)) * 100
          const endPercentage = (endInSeconds / Math.max(this.duration)) * 100
          const color = recordSegment.color
          return [startPercentage, endPercentage, { backgroundColor: color }]  // 要想多段显示不同的颜色，格式就是[开始，结束，{属性：属性值}]
        })
      } else {
        // 如果展示的是text
        return this.textSegments.map(textSegment => {
          // 将数据库中的数据转化为秒数
          const startInSeconds = this.timeToSeconds(textSegment.start)
          const endInSeconds = this.timeToSeconds(textSegment.end)

          // 计算色块所占百分比
          const startPercentage = (startInSeconds / Math.max(this.duration)) * 100
          const endPercentage = (endInSeconds / Math.max(this.duration)) * 100
          const color = textSegment.color
          return [startPercentage, endPercentage, { backgroundColor: color }]  // 要想多段显示不同的颜色，格式就是[开始，结束，{属性：属性值}]
        })
      }
    },

    // 标注
    annotation(label) {
      if (this.labelTypeMap[label] === this.finalSelectedMultiLabelType && this.selectedPersonId === '全选') {
        // 如果是全选并且选择的是小组标签
        if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
        } else if (this.isEditingTextRow !== null) {
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
              this.translateXValue = (this.currentTime / Math.max(this.duration)) * this.$refs.slider.$el.clientWidth
              this.showMark = true

              // 将当前选择的标签设为正在标注的标签
              this.currentAnnotationLabel = label

              // 创建新的标注
              this.annotationInProgress = {
                classId: this.selectedVideos[0].parent[0].id,
                groupId: this.selectedVideos[0].parent[1].id,
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
                  this.translateXValue = (this.currentTime / Math.max(this.duration)) * this.$refs.slider.$el.clientWidth
                  this.showMark = true

                  this.annotationInProgress.end = currentTime  // 更新正在标注的数据的end 
                  this.saveAnnotation(this.annotationInProgress)  // 存入数据库
                  this.currentAnnotationLabel = label // 将当前选择的标签设为正在标注的标签

                  // 创建新的标注
                  this.annotationInProgress = {
                    classId: this.selectedVideos[0].parent[0].id,
                    groupId: this.selectedVideos[0].parent[1].id,
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
      } else if (this.labelTypeMap[label] === this.finalSelectedSingleLabelType && this.selectedPersonId !== '全选' && this.selectedPersonId !== null) {
        // 如果非全选并且选择的是个人标签且不为空
        if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
        } else if (this.isEditingTextRow !== null) {
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
              this.translateXValue = (this.currentTime / Math.max(this.duration)) * this.$refs.slider.$el.clientWidth
              this.showMark = true

              // 将当前选择的标签设为正在标注的标签
              this.currentAnnotationLabel = label

              // 创建新的标注
              this.annotationInProgress = {
                classId: this.selectedVideos[0].parent[0].id,
                groupId: this.selectedVideos[0].parent[1].id,
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
                  this.translateXValue = (this.currentTime / Math.max(this.duration)) * this.$refs.slider.$el.clientWidth
                  this.showMark = true

                  this.annotationInProgress.end = currentTime  // 更新正在标注的数据的end 
                  this.saveAnnotation(this.annotationInProgress)  // 存入数据库
                  this.currentAnnotationLabel = label // 将当前选择的标签设为正在标注的标签

                  // 创建新的标注
                  this.annotationInProgress = {
                    classId: this.selectedVideos[0].parent[0].id,
                    groupId: this.selectedVideos[0].parent[1].id,
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
      } else {
        this.$message.error("未选择被标注者或正确的标签组按钮")
      }
    },

    // 保存标记
    saveAnnotation(annotation) {
      // 将标注数据保存到数据库
      if (this.selectedPersonId === '全选') {
        // 存小组标签
        this.$request.post("/groupRecord/createGroupRecord", annotation).then(() => {
          // 刷新record
          this.showRecord()
        })
      } else {
        // 存个人标签
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
      } else if (this.isEditingTextRow !== null) {
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

          // 如果不合法
          if (!isGood) {
            this.$message.error("请检查修改数据合法性")
            row.start = this.firstStart
            row.end = this.firstEnd
            row.label = this.firstLabel
          } else {
            // 更新当前行数据
            this.$request.post("/groupRecord/updateGroupRecord", {
              id: row.id,
              classId: row.classId,
              groupId: row.groupId,
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

          // 如果不合法
          if (!isGood) {
            this.$message.error("请检查修改数据合法性")
            row.start = this.firstStart
            row.end = this.firstEnd
            row.label = this.firstLabel
          } else {
            // 更新当前行数据
            this.$request.post("/record/updateRecord", {
              id: row.id,
              classId: row.classId,
              groupId: row.groupId,
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
        } else if (this.isEditingTextRow !== null) {
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
        } else if (this.isEditingTextRow !== null) {
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
      if (this.selectedVideoIndex === 0) {
        // 多视频
        for (let i = 0; i < this.selectedVideos.length; i++) {
          this.$refs[`videoPlayer${i+1}`].player.currentTime(this.timeToSeconds(time))
        }
      } else {
        // 选择单个视频则只需要将视频当前时间置为进度条的进度即可
        this.$refs[`videoPlayer${this.selectedVideoIndex}`].player.currentTime(this.timeToSeconds(time))
      }
    },

    // 获取表格中标签的展示颜色
    getLabelColor(label) {
      // 根据label设置this.buttonColors
      if (this.selectedPersonId === '全选') {
        return this.buttonColors[this.finalSelectedMultiLabelType].find(item => item.label === label).color
      } else {
        return this.buttonColors[this.finalSelectedSingleLabelType].find(item => item.label === label).color
      }
    },

    ///////////////// 画布和画框 /////////////////
    // 监听画框
    drawKeyDown(event) {
      if (event.key === 'd') {  // 按下d键开始结束画框
        this.startDraw()
      }
    },

    // 监听删除矩形框
    deleteRect(event) {
      // 按键s删除框
      if (event.key === 's' && this.canvas[this.selectedVideoIndex].getActiveObject()) {
        // 拿到当前选择的矩形框对象
        const selectedObject = this.canvas[this.selectedVideoIndex].getActiveObject()

        // 从画布中先移除选中矩形框
        this.canvas[this.selectedVideoIndex].remove(selectedObject)
        this.canvas[this.selectedVideoIndex].renderAll()

        // 删除this.rects和this.faces对应数据...this.rects[videoIndex][personId]
        delete this.rects[this.selectedVideoIndex][selectedObject.personId][this.formatTooltip(this.currentTime)]
        
        let closestFace = null
        for (const face of this.faces[this.selectedVideoIndex]) {
          if (face.personId === selectedObject.personId && face.time <= this.formatTooltip(this.currentTime)) {
            // 如果最近的矩形信息不存在或者当前矩形的时间更接近当前时间，则更新最近的矩形信息
            if (!closestFace || face.time >= closestFace.time) {
              closestFace = face
            }
          }
        }

        if (closestFace) {  // 如果找到最近的
          // 删除后台数据
          this.$request.delete("/face/deleteFace", {
            params: {
              videoName: closestFace.videoName,
              personId: closestFace.personId,
              uid: closestFace.uid,
              time: closestFace.time
            }
          })
        }
      }       
    }, 

    // 绑定监听函数
    bindCanvasOn(videoIndex) {
      // 监听fabric画布事件以及删除矩形框键盘监听事件
      this.canvas[videoIndex].on('mouse:down', this.mouseDown)
      this.canvas[videoIndex].on('mouse:up', this.mouseUp)
      this.canvas[videoIndex].on('object:moving', this.objectMoving)
      this.canvas[videoIndex].on('object:scaling', this.objectScaling)
      this.canvas[videoIndex].on('object:rotating', this.objectRotating)
      this.canvas[videoIndex].on('selection:created', this.selectionCreated)
      this.canvas[videoIndex].on('selection:updated', this.selectionUpdated)
      document.addEventListener('keydown', this.deleteRect)
    },

    // 开始画框
    startDraw() {
      const videoIndex = this.selectedVideoIndex
      if (this.selectedVideos === null) {
        this.$message.error("还未选择视频")
      } else if (videoIndex === 0) {
        this.$message.error("请选择单个视频画框")
      } else if (this.annotationInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.annotationTextInProgress !== null) {
        this.$message.error("请先完成当前标注")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else if (!this.selectedPersonId) {
        this.$message.error("请选择被标注者")
      } else if (this.selectedPersonId === '全选') {
        this.$message.error("请选择单个被标注者")
      } else if (!this.selectedRect) {
        this.$message.error("请选择矩形框")
      } else if (this.selectedPersonId !== this.selectedRect && this.selectedRect !== '全选') {
        this.$message.error("请选择相同的被标注者")
      } else {
        try {
          if (this.$refs[`videoPlayer${videoIndex}`].player.paused() === false) {
            this.$message.error("请先暂停视频才能开始画框")
          } else {
            // 切换startDrawMode
            this.startDrawMode = !this.startDrawMode
            
            if (this.startDrawMode === true) {  // 开始画框
              this.$message.success("开始画框")
              // 将当前选择的播放器的画布置为不可穿透  
              document.querySelector(`.canvas-container${videoIndex}`).style.pointerEvents = 'auto'
              
              // 监听函数
              this.bindCanvasOn(videoIndex)
            } else {  // 结束画框
              this.$message.success("结束画框")
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
        } catch (error) {
          console.log(error)
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
          this.faces[videoIndex] = res.data
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
          this.faces[videoIndex] = res.data
        }
      } else {  // 如果播放器是全选
        if (this.selectedRect === '全选') {
          for (let i = 1; i <= this.selectedVideos.length; i++) {
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
        } else {
          for (let i = 1; i <= this.selectedVideos.length; i++) {
            // 设置参数params
            const params = {
              videoName: this.videoNames[i-1],
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
        for (const personId of this.personIds) {
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
              stroke: this.colorMap[closestFace.personId],
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
        for (let i = 1; i <= this.selectedVideos.length; i++) {
          // 清除画布上的所有对象
          this.canvas[i].getObjects().forEach(obj => {
            this.canvas[i].remove(obj)
          })

          // 遍历所有的person_id，这里拿this.personIds获取所有person_id
          for (const personId of this.personIds) {
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
                stroke: this.colorMap[closestFace.personId],
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
            stroke: this.colorMap[this.selectedPersonId],
            strokeWidth: 0.5,
            strokeUniform: true,
            personId: this.selectedPersonId,
            uid: this.user.id,
          })

          // 加入画布、重新渲染
          this.canvas[videoIndex].add(this.rects[videoIndex][this.selectedPersonId][currentTime])
          this.canvas[videoIndex].renderAll()
        }
      }
    },

    // 鼠标操作矩形框改变时触发的函数
    objectMoving() {
      this.isDrawing = false  // 说明不再触发绘画动作
    },
    objectScaling() {
      this.isDrawing = false  // 说明不再触发绘画动作
    },
    objectRotating() {
      this.isDrawing = false  // 说明不再触发绘画动作
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

    ///////////////// 导出数据 /////////////////
    exportRecordAndText() {  // 导出数据
      if (this.showRecordTable === true) {  // 导出record
        if (this.isEditingRow !== null) {
          this.$message.error("请先完成修改")
        } else if (this.startDrawMode === true) {
          this.$message.error("请先结束画框")
        } else if (this.recordData.length === 0) {
          this.$message.error("没有数据")
        } else {
          if (this.selectedPersonId === '全选') {
            // 根据这些来导出
            const params = {
              token: this.user.token,
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              uid: this.user.id,
              labelType: this.finalSelectedMultiLabelType,
              className: this.selectedVideos[0].parent[0].label,
              groupName: this.selectedVideos[0].parent[1].label,
            }

            // 构建导出的URL
            const exportURL = "http://" + this.ip + ":8888/groupRecord/exportGroupRecord?" + Object.entries(params).map(([key, value]) => key + '=' + value).join('&')

            // 打开exportURL对应的导出文件的弹窗
            window.open(exportURL)
          } else {
            // 根据这些来导出
            const params = {
              token: this.user.token,
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              personId: this.selectedPersonId,
              uid: this.user.id,
              labelType: this.finalSelectedSingleLabelType,
              className: this.selectedVideos[0].parent[0].label,
              groupName: this.selectedVideos[0].parent[1].label,
              personName: this.personIdNameMap[this.selectedPersonId],
            }

            // 构建导出的URL
            const exportURL = "http://" + this.ip + ":8888/record/exportRecord?" + Object.entries(params).map(([key, value]) => key + '=' + value).join('&')

            // 打开exportURL对应的导出文件的弹窗
            window.open(exportURL)
          }
        }
      } else {
        if (this.isEditingTextRow !== null) {
          this.$message.error("请先完成修改")
        } else if (this.startDrawMode === true) {
          this.$message.error("请先结束画框")
        } else if (this.textData.length === 0) {
          this.$message.error("没有数据")
        } else {
          if (this.selectedText === '全选') {
            // 根据这些来导出
            const params = {
              token: this.user.token,
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              uid: this.user.id,
              className: this.selectedVideos[0].parent[0].label,
              groupName: this.selectedVideos[0].parent[1].label,
            }

            // 构建导出的URL
            const exportURL = "http://" + this.ip + ":8888/text/exportText1?" + Object.entries(params).map(([key, value]) => key + '=' + value).join('&')

            // 打开exportURL对应的导出文件的弹窗
            window.open(exportURL)
          } else {
            // 根据这些来导出
            const params = {
              token: this.user.token,
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              personId: this.selectedText,
              uid: this.user.id,
              className: this.selectedVideos[0].parent[0].label,
              groupName: this.selectedVideos[0].parent[1].label,
              personName: this.personIdNameMap[this.selectedText],
            }

            // 构建导出的URL
            const exportURL = "http://" + this.ip + ":8888/text/exportText2?" + Object.entries(params).map(([key, value]) => key + '=' + value).join('&')

            // 打开exportURL对应的导出文件的弹窗
            window.open(exportURL)
          }
        }
      }
    },

    ///////////////// 文本和字幕 /////////////////
    // 展示text数据表格
    showText() {
      if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        if (this.selectedText === null) {
          return
        } else if (this.selectedText === '') {
          // 如果点击clear按钮则清空数据
          this.textData = []
          this.textSegments = []
        } else if (this.selectedText === '全选') {
          this.showRecordTable = false
          this.textSegments = []  // 全选则不展示彩带，因为会有重合
          this.finalSelectedSingleLabelType = null
          this.finalSelectedMultiLabelType = null
          this.$request.get("/text/getAllText", {
            params: {
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              uid: this.user.id,
            }
          }).then(res => {
            this.textData = res.data
            this.showTextInCanvas()
          })
        } else {
          this.showRecordTable = false
          this.finalSelectedSingleLabelType = null
          this.finalSelectedMultiLabelType = null
          this.$request.get("/text/getText", {
            params: {
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              personId: this.selectedText,
              uid: this.user.id,
            }
          }).then(res => {
            this.textData = res.data
            this.createTextSegments(this.textData)
            this.showTextInCanvas()
          })
        }
      }
    },

    // 创建textSegments数组用于后续显示彩带
    createTextSegments(textData) {
      this.textSegments = textData.map(textSegment => ({
        start: textSegment.start,
        end: textSegment.end,
        color: 'yellow'
      }))
    },

    // 标注文本
    annotationText() {
      if (!this.selectedText) {
        this.$message.error("请选择文本中被标注者")
      } else if (this.selectedText === '全选') {
        this.$message.error("请选择单个被标注者")
      } else if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.isEditingRow !== null) {
        this.$message.error("请先完成修改")
      } else if (this.startDrawMode === true) {
        this.$message.error("请先结束画框")
      } else {
        //获取当前视频播放时间
        const currentTime = this.formatTooltip(this.currentTime)

        // 判断当前时间是否与已标记的片段重叠
        const isOverlapping = this.textSegments.some(textSegment => {
          const startInSeconds = this.timeToSeconds(textSegment.start)
          const endInSeconds = this.timeToSeconds(textSegment.end)
          return this.currentTime > startInSeconds && this.currentTime < endInSeconds
        })

        if (isOverlapping) {  // 有重叠
          this.$message.error("标注位置有重叠")
        } else {
          if (!this.annotationTextInProgress) {  // 如果没有正在标注的数据
            // 显示高亮标记（竖黑线）
            this.translateXValue = (this.currentTime / Math.max(this.duration)) * this.$refs.slider.$el.clientWidth
            this.showMark = true

            // 创建新的标注
            this.annotationTextInProgress = {
              classId: this.selectedVideos[0].parent[0].id,
              groupId: this.selectedVideos[0].parent[1].id,
              personId: this.selectedText,
              uid: this.user.id,
              start: currentTime,  // 开始时间为currentTime
              end: null,           // 结束时间暂时为空
              text: ''  
            }
          } else {
            if (this.currentTime <= this.timeToSeconds(this.annotationTextInProgress.start)) {
              this.$message.error("结束时间不能在开始时间之前")
            } else {
              // 如果此时即将插入数据库的新标注片段的开始时间和结束时间没有覆盖原有的片段
              const isCovering = this.textSegments.some(textSegment => {
                const startInSeconds = this.timeToSeconds(textSegment.start)
                const endInSeconds = this.timeToSeconds(textSegment.end)
                return startInSeconds >= this.timeToSeconds(this.annotationTextInProgress.start) && endInSeconds <= this.currentTime
              })

              if (isCovering) {  // 有覆盖
                this.$message.error("新标注片段覆盖了原片段")
              } else {
                // 隐藏高亮标记（竖黑线）
                this.showMark = false
                this.annotationTextInProgress.end = currentTime  // 更新正在标注的数据的end

                // 存入数据库之前创建fabric.Textbox
                let Textbox = new fabric.Textbox(this.personIdNameMap[this.selectedText] + ": ", {
                  width: this.width * 2,
                  fontSize: 20,
                  fill: 'white',
                  textAlign: 'center', // 设置文本居中对齐
                  splitByGrapheme: true, // 允许按字符进行换行
                  personId: this.selectedText,
                })

                Textbox.set({
                  top: this.height * 2 - Textbox.height,
                })
                this.canvasText.add(Textbox)
                this.canvasText.renderAll()  // 重新渲染
                
                this.saveAnnotationText(this.annotationTextInProgress)  // 存入数据库
                this.annotationTextInProgress = null  // 清空正在标注的数据
              }
            }
          }
        }
      } 
    },

    // 保存文本
    saveAnnotationText(annotationText) {
      // 将标注数据保存到数据库
      this.$request.post("/text/createText", annotationText).then(() => {
        // 刷新record
        this.showText()
      })
    },

    // 更新text
    updateText(index, row) {
      if (this.isEditingTextRow === null) {  // 如果是第一次点击编辑，开启编辑模式
        // 赋初值
        this.isEditingTextRow = index
        this.firstTextStart = row.start
        this.firstTextEnd = row.end
        this.firstText = row.text
      } else if (this.isEditingTextRow === index) {  // 如果是第二次点击编辑，即可把修改的数据保存
        // 把正在编辑设为null
        this.isEditingTextRow = null

        // 先检查修改的数据合不合法
        const isGood = this.isTextGood(row)

        // 如果不合法
        if (!isGood) {
          this.$message.error("请检查修改数据合法性")
          row.firstTextStart = this.firstTextStart
          row.firstTextEnd = this.firstTextEnd
          row.firstText = this.firstText
        } else {
          this.$request.post("/text/updateText", {
            id: row.id,
            classId: row.classId,
            groupId: row.groupId,
            personId: row.personId,
            uid: row.uid,
            start: row.start,
            end: row.end,
            text: row.text,
          }).then(() => {
            // 刷新record
            this.showText()
          })
        }
      } else {
        this.$message.error("请先修改完当前行")
      }
    },

    // 检查更新数据合法性
    isTextGood(row) {
      // 备份原始 recordSegments
      let originalSegments = [...this.textSegments]

      // 找到当前行对应originalSegments里的下标
      const rowIndex = originalSegments.findIndex(segment => {
        return segment.start === this.firstTextStart && segment.end === this.firstTextEnd
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

    // 删除text
    deleteText(row) {
      if (this.isEditingTextRow !== null) {
        this.$message.error("请先完成修改")
      } else {
        this.$request.delete("/text/deleteText/" + row.id).then(() => {
          // 删除完，刷新一遍record
          this.showText()
        })
      }
    },

    // 展示字幕
    showTextInCanvas() {
      // 获取当前时间
      const currentTime = this.formatTooltip(this.currentTime)

      // 清除画布上的所有对象
      this.canvasText.getObjects().forEach(obj => {
        this.canvasText.remove(obj)
      })

      if (this.selectedText === '全选') {
        let topOffset = 0  // 初始位移
        for (let personId of this.personIds) {
          // 过滤出每个personId对应的文本数据
          let filteredTextData = this.textData.filter(text => text.personId === personId)
          for (const text of filteredTextData) {
            if (currentTime >= text.start && currentTime <= text.end) {
              // 找符合当前时间的数据渲染到画布
              let Textbox = new fabric.Textbox(this.personIdNameMap[text.personId] + ": " + text.text, {
                width: this.width * 2,
                fontSize: 20,
                fill: 'white',
                textAlign: 'center', // 设置文本居中对齐
                splitByGrapheme: true, // 允许按字符进行换行
                personId: text.personId,
              })

              Textbox.set({
                top: (this.height * 2) - Textbox.height - topOffset,
              })
              this.canvasText.add(Textbox)
              this.canvasText.renderAll()  // 重新渲染
              topOffset += Textbox.height  // 增大位移
            }
          }
        }
      } else {
        for (const text of this.textData) {
          // 单个直接遍历即可
          if (currentTime >= text.start && currentTime <= text.end) {
            let Textbox = new fabric.Textbox(this.personIdNameMap[text.personId] + ": " + text.text, {
              width: this.width * 2,
              fontSize: 20,
              fill: 'white',
              textAlign: 'center', // 设置文本居中对齐
              splitByGrapheme: true, // 允许按字符进行换行
              personId: text.personId,
            })
            
            Textbox.set({
              top: this.height * 2 - Textbox.height,
            })
            this.canvasText.add(Textbox)
            this.canvasText.renderAll()  // 重新渲染
          }
        }
      }
    },

    // 触发标记文本和字幕
    textKeyDown(event) {
      if (event.key === 'a') {
        // 按键为a则开始标记文本
        this.annotationText()
      }
    },
  }
}
</script>

<style scoped>
  .video-players-container {
    position: relative;
    display: grid;
    grid-template-columns: 1fr 1fr;
    /* gap: 5px; */
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
  .canvas-container-text {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 3; /* 设置为比视频和之前的画布更高的 z-index */
    pointer-events: none; /* 选择是否允许画布接收鼠标事件 */
  }
  .canvas-container1-label {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 4;
    pointer-events: none;
  }
  .canvas-container2-label {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 4;
    pointer-events: none;
  }
  .canvas-container3-label {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 4;
    pointer-events: none;
  }
  .canvas-container4-label {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 4;
    pointer-events: none;
  }
</style>