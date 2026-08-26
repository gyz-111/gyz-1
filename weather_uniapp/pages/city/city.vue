<template>
  <view class="container">
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input class="search-input" placeholder="搜索城市" v-model="searchText" @input="doSearch" />
        <text class="clear-btn" v-if="searchText" @click="clearSearch">✕</text>
      </view>
    </view>

    <view class="city-list" v-if="!searchText">
      <view class="section-title">热门城市</view>
      <view class="city-grid">
        <view class="city-grid-item" v-for="(city, index) in hotCities" :key="index" @click="selectCity(city.name)">
          <text>{{ city.name }}</text>
        </view>
      </view>

      <view class="section-title">河南省</view>
      <view class="city-item" @click="selectCity('洛阳')">
        <text class="city-name">洛阳</text>
        <text class="city-count">14个区县</text>
      </view>
      <view class="district-list">
        <view class="district-item" v-for="(district, index) in luoyangDistricts" :key="index" @click="selectCity(district)">
          <text>{{ district }}</text>
        </view>
      </view>

      <view class="city-item" @click="selectCity('三门峡')">
        <text class="city-name">三门峡</text>
        <text class="city-count">6个区县</text>
      </view>
      <view class="district-list">
        <view class="district-item" v-for="(district, index) in sanmenxiaDistricts" :key="index" @click="selectCity(district)">
          <text>{{ district }}</text>
        </view>
      </view>

      <view class="section-title">其他城市</view>
      <view class="city-item" v-for="(city, index) in otherCities" :key="index" @click="selectCity(city.name)">
        <text class="city-name">{{ city.name }}</text>
      </view>
    </view>

    <view class="search-list" v-else>
      <view class="search-item" v-for="(city, index) in searchResult" :key="index" @click="selectCity(city.name)">
        <text class="search-icon">📍</text>
        <text class="search-name">{{ city.name }}</text>
      </view>
      <view class="no-result" v-if="searchResult.length === 0">
        <text>未找到相关城市</text>
      </view>
    </view>
  </view>
</template>

<script>
const cityDB = [
  { name: '北京', lon: 116.4074, lat: 39.9042 },
  { name: '上海', lon: 121.4737, lat: 31.2304 },
  { name: '广州', lon: 113.2644, lat: 23.1291 },
  { name: '深圳', lon: 114.0579, lat: 22.5431 },
  { name: '郑州', lon: 113.6253, lat: 34.7466 },
  { name: '洛阳', lon: 112.4536, lat: 34.6234 },
  { name: '三门峡', lon: 111.1921, lat: 34.7679 },
  { name: '老城区', lon: 112.4780, lat: 34.6280 },
  { name: '西工区', lon: 112.4680, lat: 34.6380 },
  { name: '瀍河区', lon: 112.5080, lat: 34.6380 },
  { name: '涧西区', lon: 112.4180, lat: 34.6280 },
  { name: '偃师区', lon: 112.7580, lat: 34.7080 },
  { name: '孟津区', lon: 112.4380, lat: 34.7880 },
  { name: '新安县', lon: 112.1880, lat: 34.7680 },
  { name: '栾川县', lon: 111.6880, lat: 33.7980 },
  { name: '嵩县', lon: 112.0580, lat: 34.1480 },
  { name: '汝阳县', lon: 112.4380, lat: 34.1880 },
  { name: '宜阳县', lon: 112.1380, lat: 34.4480 },
  { name: '洛宁县', lon: 111.6880, lat: 34.3980 },
  { name: '伊川县', lon: 112.5380, lat: 34.4980 },
  { name: '湖滨区', lon: 111.2080, lat: 34.7780 },
  { name: '陕州区', lon: 111.1380, lat: 34.7680 },
  { name: '渑池县', lon: 111.7580, lat: 34.7980 },
  { name: '卢氏县', lon: 110.8080, lat: 34.0980 },
  { name: '义马市', lon: 111.9380, lat: 34.7880 },
  { name: '灵宝市', lon: 110.8580, lat: 34.5280 }
]

export default {
  data() {
    return {
      searchText: '',
      searchResult: [],
      hotCities: [
        { name: '北京' },
        { name: '上海' },
        { name: '广州' },
        { name: '深圳' },
        { name: '郑州' },
        { name: '洛阳' }
      ],
      luoyangDistricts: [
        '老城区', '西工区', '瀍河区', '涧西区',
        '偃师区', '孟津区', '新安县', '栾川县',
        '嵩县', '汝阳县', '宜阳县', '洛宁县', '伊川县'
      ],
      sanmenxiaDistricts: [
        '湖滨区', '陕州区', '渑池县', '卢氏县', '义马市', '灵宝市'
      ],
      otherCities: [
        { name: '上海' },
        { name: '广州' },
        { name: '深圳' },
        { name: '郑州' }
      ]
    }
  },
  methods: {
    doSearch() {
      var text = this.searchText.trim()
      if (!text) {
        this.searchResult = []
        return
      }
      this.searchResult = cityDB.filter(function(item) {
        return item.name.indexOf(text) !== -1
      })
    },
    clearSearch() {
      this.searchText = ''
      this.searchResult = []
    },
    selectCity(name) {
      var foundCity = null
      for (var i = 0; i < cityDB.length; i++) {
        if (cityDB[i].name === name) {
          foundCity = cityDB[i]
          break
        }
      }
      if (foundCity) {
        uni.setStorageSync('selectedCity', foundCity)
      }
      uni.navigateBack()
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
}

.search-bar {
  padding: 20rpx;
  background: rgba(26, 26, 46, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 30rpx;
  padding: 15rpx 25rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
}

.search-icon {
  font-size: 28rpx;
  margin-right: 15rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #fff;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.clear-btn {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.5);
  padding: 0 10rpx;
}

.city-list {
  padding: 20rpx;
}

.section-title {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 15rpx;
  padding-left: 10rpx;
}

.city-grid {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 30rpx;
}

.city-grid-item {
  width: 33.33%;
  padding: 25rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #fff;
}

.city-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 15rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.05);
  margin-bottom: 10rpx;
}

.city-name {
  font-size: 30rpx;
  color: #fff;
}

.city-count {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.4);
}

.district-list {
  display: flex;
  flex-wrap: wrap;
  padding: 10rpx 0 25rpx 0;
}

.district-item {
  width: 33.33%;
  padding: 18rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.search-list {
  padding: 20rpx;
}

.search-item {
  display: flex;
  align-items: center;
  padding: 25rpx 15rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.05);
  margin-bottom: 10rpx;
}

.search-item .search-icon {
  font-size: 28rpx;
  margin-right: 20rpx;
}

.search-name {
  font-size: 30rpx;
  color: #fff;
}

.no-result {
  text-align: center;
  padding: 80rpx 0;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.4);
}
</style>
