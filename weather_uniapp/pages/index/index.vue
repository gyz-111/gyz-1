<template>
  <view class="container">
    <view class="header">
      <view class="city-area" @click="goCity">
        <text class="city-name">{{ currentCity }}</text>
        <text class="arrow">></text>
      </view>
      <view class="header-right">
        <view class="location-btn" @click="getLocation">
          <text class="location-icon">📍</text>
        </view>
        <view class="refresh-btn" @click="getWeather">
          <text class="refresh-icon">↻</text>
        </view>
      </view>
    </view>

    <view class="weather-card">
      <view class="weather-icon-lg">
        <text>{{ getWeatherIcon(weatherData.text) }}</text>
      </view>
      <view class="temp-section">
        <text class="temp-value">{{ weatherData.temp }}°</text>
        <text class="weather-text">{{ weatherData.text }}</text>
      </view>
      <view class="feels-like">
        <text>体感 {{ weatherData.feelsLike }}°C</text>
      </view>
    </view>

    <view class="info-grid">
      <view class="info-item">
        <view class="info-icon-box">💧</view>
        <text class="info-label">湿度</text>
        <text class="info-value">{{ weatherData.humidity }}%</text>
      </view>
      <view class="info-item">
        <view class="info-icon-box">💨</view>
        <text class="info-label">风速</text>
        <text class="info-value">{{ weatherData.windDir }} {{ weatherData.windScale }}级</text>
      </view>
      <view class="info-item">
        <view class="info-icon-box">👁️</view>
        <text class="info-label">能见度</text>
        <text class="info-value">{{ weatherData.vis }}km</text>
      </view>
      <view class="info-item">
        <view class="info-icon-box">🌡️</view>
        <text class="info-label">气压</text>
        <text class="info-value">{{ weatherData.pressure }}hPa</text>
      </view>
    </view>

    <view class="forecast-card">
      <view class="card-header">
        <text class="card-title">未来7天</text>
      </view>
      <scroll-view class="forecast-scroll" scroll-x>
        <view class="forecast-list">
          <view class="forecast-item" v-for="(item, index) in forecastList" :key="index">
            <text class="forecast-date">{{ formatDate(item.fxDate) }}</text>
            <view class="forecast-icon-sm">
              <text>{{ getWeatherIcon(item.textDay) }}</text>
            </view>
            <text class="forecast-high">{{ item.tempMax }}°</text>
            <text class="forecast-low">{{ item.tempMin }}°</text>
            <view class="forecast-precip">
              <text v-if="item.precip > 0">{{ item.precip }}mm</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="precipitation-card">
      <view class="card-header">
        <text class="card-title">降水预估</text>
      </view>
      <view class="precipitation-chart">
        <view class="chart-y-axis">
          <text v-for="(label, index) in yAxisLabels" :key="index">{{ label }}</text>
        </view>
        <view class="chart-bars">
          <view class="bar-item" v-for="(item, index) in forecastList" :key="index">
            <view class="bar-wrapper">
              <view class="bar" :style="{ height: getBarHeight(item.precip) + '%', backgroundColor: getBarColor(item.precip) }"></view>
            </view>
            <text class="bar-label">{{ formatDate(item.fxDate) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="warning-card" v-if="warningList.length > 0">
      <view class="card-header">
        <text class="card-title">⚠️ 天气预警</text>
      </view>
      <view class="warning-list">
        <view class="warning-item" v-for="(item, index) in warningList" :key="index">
          <view class="warning-tag" :style="{ backgroundColor: getLevelColor(item.level) }">
            {{ item.level }}
          </view>
          <text class="warning-title">{{ item.title }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
const API_KEY = 'c22b01d72bbe469cb77699532913ff67'
const API_HOST = 'nr7qquf7hj.re.qweatherapi.com'

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
      currentCity: '北京',
      cityLon: 116.4074,
      cityLat: 39.9042,
      weatherData: {
        temp: '--',
        text: '加载中',
        feelsLike: '--',
        humidity: '--',
        windDir: '--',
        windScale: '--',
        vis: '--',
        pressure: '--'
      },
      forecastList: [],
      warningList: [],
      yAxisLabels: ['20', '15', '10', '5', '0']
    }
  },
  onLoad(options) {
    var savedCity = uni.getStorageSync('selectedCity')
    if (savedCity) {
      this.currentCity = savedCity.name
      this.cityLon = savedCity.lon
      this.cityLat = savedCity.lat
    }
    this.getWeather()
    this.getLocation()
  },
  onShow() {
    var savedCity = uni.getStorageSync('selectedCity')
    if (savedCity && savedCity.name !== this.currentCity) {
      this.currentCity = savedCity.name
      this.cityLon = savedCity.lon
      this.cityLat = savedCity.lat
      this.getWeather()
    }
  },
  methods: {
    findCity(name) {
      for (var i = 0; i < cityDB.length; i++) {
        if (cityDB[i].name === name) {
          return cityDB[i]
        }
      }
      return null
    },
    goCity() {
      uni.navigateTo({
        url: '/pages/city/city'
      })
    },
    selectCity(name) {
      var city = this.findCity(name)
      if (city) {
        this.currentCity = city.name
        this.cityLon = city.lon
        this.cityLat = city.lat
        uni.setStorageSync('selectedCity', city)
        this.getWeather()
      }
    },
    getLocation() {
      var that = this
      uni.getLocation({
        type: 'gcj02',
        success: function(res) {
          that.cityLat = res.latitude
          that.cityLon = res.longitude
          that.reverseGeocode(res.latitude, res.longitude)
        },
        fail: function(err) {
          console.log('获取位置失败:', err)
        }
      })
    },
    reverseGeocode(lat, lon) {
      var that = this
      uni.request({
        url: 'https://apis.map.qq.com/ws/geocoder/v1/',
        data: {
          location: lat + ',' + lon,
          key: 'OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77'
        },
        success: function(res) {
          if (res.data && res.data.status === 0) {
            var city = res.data.result.address_component.city
            if (city && city.length > 0) {
              if (city.endsWith('市')) {
                city = city.substring(0, city.length - 1)
              }
              var foundCity = that.findCity(city)
              if (foundCity) {
                that.currentCity = foundCity.name
                that.cityLat = foundCity.lat
                that.cityLon = foundCity.lon
                uni.setStorageSync('selectedCity', foundCity)
                that.getWeather()
              }
            }
          }
        }
      })
    },
    getWeather() {
      var that = this
      uni.showLoading({ title: '加载中...' })
      
      var location = this.cityLon + ',' + this.cityLat
      
      var nowUrl = 'https://' + API_HOST + '/v7/weather/now?location=' + location + '&key=' + API_KEY
      
      uni.request({
        url: nowUrl,
        method: 'GET',
        success: function(res) {
          if (res.data && res.data.code === '200') {
            var now = res.data.now
            that.weatherData = {
              temp: now.temp,
              text: now.text,
              feelsLike: now.feelsLike,
              humidity: now.humidity,
              windDir: now.windDir || '--',
              windScale: now.windScale || '--',
              vis: now.vis || '--',
              pressure: now.pressure || '--'
            }
          } else {
            that.weatherData.text = '获取失败'
          }
        },
        fail: function() {
          that.weatherData.text = '网络错误'
        },
        complete: function() {
          uni.hideLoading()
        }
      })
      
      var forecastUrl = 'https://' + API_HOST + '/v7/weather/7d?location=' + location + '&key=' + API_KEY
      uni.request({
        url: forecastUrl,
        method: 'GET',
        success: function(res) {
          if (res.data && res.data.code === '200') {
            that.forecastList = res.data.daily || []
          }
        }
      })
      
      var warningUrl = 'https://' + API_HOST + '/v7/warning/now?location=' + location + '&key=' + API_KEY
      uni.request({
        url: warningUrl,
        method: 'GET',
        success: function(res) {
          if (res.data && res.data.code === '200') {
            that.warningList = res.data.warning || []
          }
        }
      })
    },
    getWeatherIcon(text) {
      if (text.indexOf('晴') !== -1) return '☀️'
      if (text.indexOf('云') !== -1) return '☁️'
      if (text.indexOf('阴') !== -1) return '⛅'
      if (text.indexOf('雨') !== -1) return '🌧️'
      if (text.indexOf('雪') !== -1) return '❄️'
      if (text.indexOf('雷') !== -1) return '⛈️'
      if (text.indexOf('雾') !== -1) return '🌫️'
      return '☀️'
    },
    getLevelColor(level) {
      if (level.indexOf('红') !== -1) return '#dc3545'
      if (level.indexOf('橙') !== -1) return '#fd7e14'
      if (level.indexOf('黄') !== -1) return '#ffc107'
      if (level.indexOf('蓝') !== -1) return '#007bff'
      return '#6c757d'
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      var date = new Date(dateStr)
      var month = date.getMonth() + 1
      var day = date.getDate()
      return month + '/' + day
    },
    getBarHeight(precip) {
      var p = parseFloat(precip) || 0
      var max = 20
      var height = (p / max) * 100
      return Math.max(height, 5)
    },
    getBarColor(precip) {
      var p = parseFloat(precip) || 0
      if (p >= 10) return '#dc3545'
      if (p >= 5) return '#fd7e14'
      if (p > 0) return '#007bff'
      return '#dee2e6'
    }
  }
}
</script>

<style>
.container {
  padding: 20rpx;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  padding-top: 20rpx;
}

.city-area {
  display: flex;
  align-items: center;
  padding: 15rpx 25rpx;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 30rpx;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1rpx solid rgba(255, 255, 255, 0.15);
}

.city-name {
  font-size: 34rpx;
  color: #fff;
  font-weight: 600;
}

.arrow {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.6);
  margin-left: 10rpx;
}

.header-right {
  display: flex;
  gap: 15rpx;
}

.location-btn {
  width: 68rpx;
  height: 68rpx;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1rpx solid rgba(255, 255, 255, 0.15);
}

.location-icon {
  font-size: 30rpx;
}

.refresh-btn {
  width: 68rpx;
  height: 68rpx;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1rpx solid rgba(255, 255, 255, 0.15);
}

.refresh-icon {
  font-size: 30rpx;
  color: #fff;
}

.weather-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 32rpx;
  padding: 50rpx 40rpx;
  margin-bottom: 20rpx;
  text-align: center;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);
}

.weather-icon-lg {
  font-size: 110rpx;
  margin-bottom: 25rpx;
}

.temp-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 15rpx;
}

.temp-value {
  font-size: 130rpx;
  color: #fff;
  font-weight: 200;
  line-height: 1;
  letter-spacing: -5rpx;
}

.weather-text {
  font-size: 38rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 15rpx;
  font-weight: 500;
}

.feels-like {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.55);
}

.info-grid {
  display: flex;
  flex-wrap: wrap;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 32rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);
}

.info-item {
  width: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25rpx 0;
}

.info-icon-box {
  font-size: 44rpx;
  margin-bottom: 12rpx;
}

.info-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.55);
  margin-bottom: 10rpx;
}

.info-value {
  font-size: 30rpx;
  color: #fff;
  font-weight: 500;
}

.forecast-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 32rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);
}

.card-header {
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  color: #fff;
  font-weight: 600;
}

.forecast-scroll {
  white-space: nowrap;
}

.forecast-list {
  display: inline-flex;
}

.forecast-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 35rpx;
  min-width: 100rpx;
}

.forecast-date {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.55);
  margin-bottom: 15rpx;
}

.forecast-icon-sm {
  font-size: 40rpx;
  margin-bottom: 15rpx;
}

.forecast-high {
  font-size: 30rpx;
  color: #fff;
  font-weight: 600;
  margin-bottom: 5rpx;
}

.forecast-low {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.55);
}

.forecast-precip {
  font-size: 22rpx;
  color: #007bff;
  margin-top: 8rpx;
}

.precipitation-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 32rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);
}

.precipitation-chart {
  display: flex;
  gap: 10rpx;
  padding: 20rpx 0;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 60rpx;
}

.chart-y-axis text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.45);
  text-align: right;
  padding-right: 10rpx;
}

.chart-bars {
  flex: 1;
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200rpx;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12%;
}

.bar-wrapper {
  width: 100%;
  height: 180rpx;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 70%;
  border-radius: 8rpx 8rpx 0 0;
  transition: height 0.5s ease;
}

.bar-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.55);
  margin-top: 10rpx;
}

.warning-card {
  background: rgba(255, 107, 107, 0.15);
  border-radius: 32rpx;
  padding: 25rpx;
  border: 1rpx solid rgba(255, 107, 107, 0.3);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
}

.warning-list {
  display: flex;
  flex-direction: column;
}

.warning-item {
  display: flex;
  align-items: center;
  padding: 15rpx 0;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
}

.warning-item:last-child {
  border-bottom: none;
}

.warning-tag {
  color: #fff;
  font-size: 22rpx;
  padding: 5rpx 15rpx;
  border-radius: 10rpx;
  margin-right: 15rpx;
}

.warning-title {
  font-size: 28rpx;
  color: #fff;
}
</style>
