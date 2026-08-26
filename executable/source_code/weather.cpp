#include <iostream>
#include <string>
#include <vector>
#include <iomanip>
#include <ctime>
#include <fstream>
#include <sstream>
#include <windows.h>
#include <wininet.h>
#include <zlib.h>
#include "json.hpp"

#pragma comment(lib, "wininet.lib")

using json = nlohmann::json;

struct CityInfo
{
    std::string name;
    std::string pinyin;
    double latitude;
    double longitude;
    std::string province;
    std::string type;
};

std::vector<CityInfo> cityDatabase = {
    {"北京", "beijing", 39.9042, 116.4074, "北京", "直辖市"},
    {"上海", "shanghai", 31.2304, 121.4737, "上海", "直辖市"},
    {"广州", "guangzhou", 23.1291, 113.2644, "广东", "地级市"},
    {"深圳", "shenzhen", 22.5431, 114.0579, "广东", "地级市"},
    {"杭州", "hangzhou", 30.2741, 120.1551, "浙江", "地级市"},
    {"成都", "chengdu", 30.5728, 104.0668, "四川", "地级市"},
    {"武汉", "wuhan", 30.5928, 114.3055, "湖北", "地级市"},
    {"南京", "nanjing", 32.0603, 118.7969, "江苏", "地级市"},
    {"西安", "xian", 34.3416, 108.9398, "陕西", "地级市"},
    {"重庆", "chongqing", 29.4316, 106.9123, "重庆", "直辖市"},
    
    {"郑州", "zhengzhou", 34.7466, 113.6253, "河南", "地级市"},
    {"开封", "kaifeng", 34.8016, 114.3534, "河南", "地级市"},
    {"洛阳", "luoyang", 34.6234, 112.4536, "河南", "地级市"},
    {"平顶山", "pingdingshan", 33.7402, 113.2860, "河南", "地级市"},
    {"安阳", "anyang", 36.1058, 114.3535, "河南", "地级市"},
    {"鹤壁", "hebi", 35.7470, 114.2961, "河南", "地级市"},
    {"新乡", "xinxiang", 35.3075, 113.8467, "河南", "地级市"},
    {"焦作", "jiaozuo", 35.2464, 113.2170, "河南", "地级市"},
    {"濮阳", "puyang", 35.7619, 115.0726, "河南", "地级市"},
    {"许昌", "xuchang", 34.0242, 113.8164, "河南", "地级市"},
    {"漯河", "luohe", 33.5816, 114.0177, "河南", "地级市"},
    {"三门峡", "sanmenxia", 34.7679, 111.1921, "河南", "地级市"},
    {"南阳", "nanyang", 33.0146, 112.5385, "河南", "地级市"},
    {"商丘", "shangqiu", 34.4436, 115.6596, "河南", "地级市"},
    {"信阳", "xinyang", 32.1278, 114.0477, "河南", "地级市"},
    {"周口", "zhoukou", 33.6330, 114.6237, "河南", "地级市"},
    {"驻马店", "zhumadian", 32.9865, 114.0287, "河南", "地级市"},
    
    {"中原区", "zhongyuan", 34.7680, 113.5730, "河南", "区县"},
    {"二七区", "erqi", 34.7320, 113.6330, "河南", "区县"},
    {"金水区", "jinshui", 34.7870, 113.6680, "河南", "区县"},
    {"管城回族区", "guancheng", 34.7470, 113.6880, "河南", "区县"},
    {"惠济区", "huiji", 34.8330, 113.6180, "河南", "区县"},
    {"上街区", "shangjie", 34.8030, 113.2280, "河南", "区县"},
    {"中牟县", "zhongmou", 34.7480, 114.0330, "河南", "区县"},
    {"巩义市", "gongyi", 34.7680, 112.9580, "河南", "区县"},
    {"荥阳市", "xingyang", 34.7980, 113.3580, "河南", "区县"},
    {"新密市", "xinmi", 34.5980, 113.4580, "河南", "区县"},
    {"新郑市", "xinzheng", 34.4980, 113.7580, "河南", "区县"},
    {"登封市", "dengfeng", 34.4580, 113.0580, "河南", "区县"},
    
    {"龙亭区", "longting", 34.8080, 114.3580, "河南", "区县"},
    {"顺河回族区", "shunhe", 34.8180, 114.3780, "河南", "区县"},
    {"鼓楼区", "gulou", 34.8080, 114.3680, "河南", "区县"},
    {"禹王台区", "yuwangtai", 34.7880, 114.3780, "河南", "区县"},
    {"祥符区", "xiangfu", 34.7580, 114.4080, "河南", "区县"},
    {"杞县", "qi", 34.5580, 114.7080, "河南", "区县"},
    {"通许县", "tongxu", 34.4880, 114.4680, "河南", "区县"},
    {"尉氏县", "weishi", 34.4480, 114.1580, "河南", "区县"},
    {"兰考县", "lankao", 34.6980, 114.8180, "河南", "区县"},
    
    {"西工区", "xigong", 34.6380, 112.4680, "河南", "区县"},
    {"老城区", "laocheng", 34.6280, 112.4780, "河南", "区县"},
    {"瀍河回族区", "chanhe", 34.6380, 112.5080, "河南", "区县"},
    {"涧西区", "jianxi", 34.6280, 112.4180, "河南", "区县"},
    {"偃师区", "yanshi", 34.7080, 112.7580, "河南", "区县"},
    {"孟津区", "mengjin", 34.7880, 112.4380, "河南", "区县"},
    {"新安县", "xinan", 34.7680, 112.1880, "河南", "区县"},
    {"栾川县", "luanchuan", 33.7980, 111.6880, "河南", "区县"},
    {"嵩县", "song", 34.1480, 112.0580, "河南", "区县"},
    {"汝阳县", "ruyang", 34.1880, 112.4380, "河南", "区县"},
    {"宜阳县", "yiyang", 34.4480, 112.1380, "河南", "区县"},
    {"洛宁县", "luoning", 34.3980, 111.6880, "河南", "区县"},
    {"伊川县", "yichuan", 34.4980, 112.5380, "河南", "区县"},
    
    {"新华区", "xinhua", 33.7580, 113.2980, "河南", "区县"},
    {"卫东区", "weidong", 33.7480, 113.3380, "河南", "区县"},
    {"石龙区", "shilong", 33.8980, 112.8880, "河南", "区县"},
    {"湛河区", "zhanhe", 33.7180, 113.3180, "河南", "区县"},
    {"宝丰县", "baofeng", 33.9380, 113.0880, "河南", "区县"},
    {"叶县", "ye", 33.6280, 113.4580, "河南", "区县"},
    {"鲁山县", "lushan", 33.7880, 112.8380, "河南", "区县"},
    {"郏县", "jia", 33.9780, 113.2380, "河南", "区县"},
    {"舞钢市", "wugang", 33.4180, 113.5580, "河南", "区县"},
    {"汝州市", "ruzhou", 34.1180, 112.8280, "河南", "区县"},
    
    {"文峰区", "wenfeng", 36.0880, 114.3880, "河南", "区县"},
    {"北关区", "beiguan", 36.1280, 114.3780, "河南", "区县"},
    {"殷都区", "yindu", 36.1580, 114.3280, "河南", "区县"},
    {"龙安区", "longan", 36.0680, 114.3380, "河南", "区县"},
    {"安阳县", "anyangxian", 36.1380, 114.2580, "河南", "区县"},
    {"汤阴县", "tangyin", 35.9580, 114.4380, "河南", "区县"},
    {"滑县", "hua", 35.5580, 114.5880, "河南", "区县"},
    {"内黄县", "neihuang", 36.0080, 114.8080, "河南", "区县"},
    {"林州市", "linzhou", 36.2480, 113.8280, "河南", "区县"},
    
    {"淇滨区", "qibin", 35.7580, 114.3080, "河南", "区县"},
    {"山城区", "shancheng", 35.8180, 114.1780, "河南", "区县"},
    {"鹤山区", "heshan", 35.9080, 114.1580, "河南", "区县"},
    {"浚县", "xun", 35.6880, 114.5080, "河南", "区县"},
    {"淇县", "qi", 35.6480, 114.1980, "河南", "区县"},
    
    {"红旗区", "hongqi", 35.3180, 113.8680, "河南", "区县"},
    {"卫滨区", "weibin", 35.2980, 113.8380, "河南", "区县"},
    {"凤泉区", "fengquan", 35.3680, 113.7980, "河南", "区县"},
    {"牧野区", "muye", 35.3380, 113.8980, "河南", "区县"},
    {"新乡县", "xinxiangxian", 35.2680, 113.8980, "河南", "区县"},
    {"获嘉县", "huojia", 35.2880, 113.6480, "河南", "区县"},
    {"原阳县", "yuanyang", 35.0780, 113.9680, "河南", "区县"},
    {"延津县", "yanjin", 35.1980, 114.1180, "河南", "区县"},
    {"封丘县", "fengqiu", 35.0080, 114.5080, "河南", "区县"},
    {"长垣市", "changyuan", 35.1680, 114.6880, "河南", "区县"},
    
    {"解放区", "jiefang", 35.2580, 113.2280, "河南", "区县"},
    {"中站区", "zhongzhan", 35.2880, 113.1680, "河南", "区县"},
    {"马村区", "macun", 35.2580, 113.3080, "河南", "区县"},
    {"山阳区", "shanyang", 35.2280, 113.2480, "河南", "区县"},
    {"修武县", "xiuwu", 35.3480, 113.4080, "河南", "区县"},
    {"博爱县", "boai", 35.1980, 113.0880, "河南", "区县"},
    {"武陟县", "wuzhi", 35.1580, 113.3880, "河南", "区县"},
    {"温县", "wen", 34.9880, 113.0680, "河南", "区县"},
    {"沁阳市", "qinyang", 35.0780, 112.9680, "河南", "区县"},
    {"孟州市", "mengzhou", 34.9480, 112.7880, "河南", "区县"},
    
    {"华龙区", "hualong", 35.7780, 115.0880, "河南", "区县"},
    {"清丰县", "qingfeng", 35.9080, 114.9080, "河南", "区县"},
    {"南乐县", "nanle", 36.0380, 115.2580, "河南", "区县"},
    {"范县", "fan", 35.8280, 115.5280, "河南", "区县"},
    {"台前县", "taiqian", 35.9680, 115.8280, "河南", "区县"},
    {"濮阳县", "puyangxian", 35.7080, 114.9580, "河南", "区县"},
    
    {"魏都区", "weidu", 34.0380, 113.8280, "河南", "区县"},
    {"建安区", "jianan", 34.0080, 113.7880, "河南", "区县"},
    {"鄢陵县", "yanling", 34.1180, 114.1080, "河南", "区县"},
    {"襄城县", "xiangcheng", 33.8580, 113.4580, "河南", "区县"},
    {"禹州市", "yuzhou", 34.1780, 113.4880, "河南", "区县"},
    {"长葛市", "changge", 34.2480, 113.7580, "河南", "区县"},
    
    {"源汇区", "yuanhui", 33.5980, 114.0280, "河南", "区县"},
    {"郾城区", "yancheng", 33.6280, 113.9980, "河南", "区县"},
    {"召陵区", "shaoling", 33.5880, 114.1080, "河南", "区县"},
    {"舞阳县", "wuyang", 33.4980, 113.6580, "河南", "区县"},
    {"临颍县", "linying", 33.8180, 113.9280, "河南", "区县"},
    
    {"湖滨区", "hubin", 34.7780, 111.2080, "河南", "区县"},
    {"陕州区", "shanzhou", 34.7680, 111.1380, "河南", "区县"},
    {"渑池县", "mianchi", 34.7980, 111.7580, "河南", "区县"},
    {"卢氏县", "lushi", 34.0980, 110.8080, "河南", "区县"},
    {"义马市", "yima", 34.7880, 111.9380, "河南", "区县"},
    {"灵宝市", "lingbao", 34.5280, 110.8580, "河南", "区县"},
    
    {"宛城区", "wancheng", 33.0280, 112.5680, "河南", "区县"},
    {"卧龙区", "wolong", 32.9980, 112.4980, "河南", "区县"},
    {"南召县", "nanzhao", 33.4280, 112.4880, "河南", "区县"},
    {"方城县", "fangcheng", 33.2580, 112.8080, "河南", "区县"},
    {"西峡县", "xixia", 33.3180, 111.5080, "河南", "区县"},
    {"镇平县", "zhenping", 33.0980, 112.2280, "河南", "区县"},
    {"内乡县", "neixiang", 33.0580, 111.8280, "河南", "区县"},
    {"淅川县", "xichuan", 33.1280, 111.4580, "河南", "区县"},
    {"社旗县", "sheqi", 33.1580, 112.9880, "河南", "区县"},
    {"唐河县", "tanghe", 32.7680, 112.8080, "河南", "区县"},
    {"新野县", "xinye", 32.5580, 112.3580, "河南", "区县"},
    {"桐柏县", "tongbai", 32.2880, 113.4080, "河南", "区县"},
    {"邓州市", "dengzhou", 32.6780, 112.0580, "河南", "区县"},
    
    {"睢阳区", "suiyang", 34.4580, 115.6680, "河南", "区县"},
    {"梁园区", "liangyuan", 34.4780, 115.6280, "河南", "区县"},
    {"民权县", "minquan", 34.6680, 115.1880, "河南", "区县"},
    {"睢县", "sui", 34.4180, 115.0280, "河南", "区县"},
    {"宁陵县", "ningling", 34.4780, 115.3880, "河南", "区县"},
    {"柘城县", "zhecheng", 34.0980, 115.3580, "河南", "区县"},
    {"虞城县", "yucheng", 34.4880, 115.8880, "河南", "区县"},
    {"夏邑县", "xiayi", 34.2180, 116.1580, "河南", "区县"},
    {"永城市", "yongcheng", 33.9580, 116.3880, "河南", "区县"},
    
    {"浉河区", "shihe", 32.1380, 114.0680, "河南", "区县"},
    {"平桥区", "pingqiao", 32.1080, 114.0080, "河南", "区县"},
    {"罗山县", "luoshan", 32.2280, 114.5880, "河南", "区县"},
    {"光山县", "guangshan", 32.0180, 114.9280, "河南", "区县"},
    {"新县", "xin", 31.6880, 114.8380, "河南", "区县"},
    {"商城县", "shangcheng", 31.8280, 115.3680, "河南", "区县"},
    {"固始县", "gushi", 32.1980, 115.6080, "河南", "区县"},
    {"潢川县", "huangchuan", 32.1580, 115.0280, "河南", "区县"},
    {"淮滨县", "huaibin", 32.4280, 115.4080, "河南", "区县"},
    {"息县", "xi", 32.3780, 114.7580, "河南", "区县"},
    
    {"川汇区", "chuanhui", 33.6480, 114.6380, "河南", "区县"},
    {"扶沟县", "fugou", 34.0180, 114.3580, "河南", "区县"},
    {"西华县", "xihua", 33.7980, 114.5080, "河南", "区县"},
    {"商水县", "shangshui", 33.4980, 114.5880, "河南", "区县"},
    {"沈丘县", "shenqiu", 33.2580, 115.0580, "河南", "区县"},
    {"郸城县", "dancheng", 33.6280, 115.1880, "河南", "区县"},
    {"淮阳区", "huaiyang", 33.7780, 114.8580, "河南", "区县"},
    {"太康县", "taikang", 34.0680, 114.8080, "河南", "区县"},
    {"鹿邑县", "luyi", 33.8580, 115.4580, "河南", "区县"},
    {"项城市", "xiangcheng", 33.4280, 114.9880, "河南", "区县"},
    
    {"驿城区", "yicheng", 32.9980, 114.0380, "河南", "区县"},
    {"西平县", "xiping", 33.3180, 114.0080, "河南", "区县"},
    {"上蔡县", "shangcai", 33.2580, 114.2880, "河南", "区县"},
    {"平舆县", "pingyu", 33.0580, 114.6580, "河南", "区县"},
    {"正阳县", "zhengyang", 32.7880, 114.3880, "河南", "区县"},
    {"确山县", "queshan", 32.8580, 113.9280, "河南", "区县"},
    {"泌阳县", "biyang", 32.7480, 113.4880, "河南", "区县"},
    {"汝南县", "runan", 32.9080, 114.4580, "河南", "区县"},
    {"遂平县", "suiping", 33.1580, 113.9280, "河南", "区县"},
    {"新蔡县", "xincai", 32.7580, 114.9280, "河南", "区县"},
    
    {"天津", "tianjin", 39.0842, 117.2008, "天津", "直辖市"},
    {"苏州", "suzhou", 31.2990, 120.6230, "江苏", "地级市"},
    {"无锡", "wuxi", 31.4910, 120.3110, "江苏", "地级市"},
    {"常州", "changzhou", 31.8110, 119.9710, "江苏", "地级市"},
    {"徐州", "xuzhou", 34.2210, 117.2210, "江苏", "地级市"},
    {"南通", "nantong", 31.9810, 120.8810, "江苏", "地级市"},
    {"扬州", "yangzhou", 32.3910, 119.4410, "江苏", "地级市"},
    {"镇江", "zhenjiang", 32.2010, 119.4610, "江苏", "地级市"},
    {"泰州", "taizhou", 32.4910, 120.2610, "江苏", "地级市"},
    {"盐城", "yancheng", 33.3410, 120.1310, "江苏", "地级市"},
    {"连云港", "lianyungang", 34.5910, 119.1710, "江苏", "地级市"},
    {"淮安", "huaian", 33.5910, 119.0210, "江苏", "地级市"},
    {"宿迁", "suqian", 33.9410, 118.2910, "江苏", "地级市"},
    
    {"宁波", "ningbo", 29.8710, 121.5510, "浙江", "地级市"},
    {"温州", "wenzhou", 28.0110, 120.6910, "浙江", "地级市"},
    {"嘉兴", "jiaxing", 30.7610, 120.7610, "浙江", "地级市"},
    {"湖州", "huzhou", 30.8510, 120.1110, "浙江", "地级市"},
    {"绍兴", "shaoxing", 30.0010, 120.5810, "浙江", "地级市"},
    {"金华", "jinhua", 29.0710, 119.6410, "浙江", "地级市"},
    {"衢州", "quzhou", 28.9210, 118.8810, "浙江", "地级市"},
    {"舟山", "zhoushan", 30.0110, 122.2010, "浙江", "地级市"},
    {"台州", "taizhou", 28.6610, 121.4210, "浙江", "地级市"},
    {"丽水", "lishui", 28.4510, 119.9210, "浙江", "地级市"},
    
    {"合肥", "hefei", 31.8210, 117.2210, "安徽", "地级市"},
    {"芜湖", "wuhu", 31.3310, 118.4410, "安徽", "地级市"},
    {"蚌埠", "bengbu", 32.9210, 117.3410, "安徽", "地级市"},
    {"淮南", "huainan", 32.6310, 116.9010, "安徽", "地级市"},
    {"马鞍山", "maanshan", 31.6910, 118.5110, "安徽", "地级市"},
    {"淮北", "huaibei", 33.9610, 116.7810, "安徽", "地级市"},
    {"铜陵", "tongling", 30.9310, 117.8210, "安徽", "地级市"},
    {"安庆", "anqing", 30.5310, 117.0510, "安徽", "地级市"},
    {"黄山", "huangshan", 29.7110, 118.1810, "安徽", "地级市"},
    {"滁州", "chuzhou", 32.2910, 118.3110, "安徽", "地级市"},
    {"阜阳", "fuyang", 32.8910, 115.8210, "安徽", "地级市"},
    {"宿州", "suzhou", 33.6210, 116.9710, "安徽", "地级市"},
    
    {"福州", "fuzhou", 26.0710, 119.2910, "福建", "地级市"},
    {"厦门", "xiamen", 24.4710, 118.0810, "福建", "地级市"},
    {"莆田", "putian", 25.4610, 119.0110, "福建", "地级市"},
    {"三明", "sanming", 26.2310, 117.6110, "福建", "地级市"},
    {"泉州", "quanzhou", 24.8910, 118.6510, "福建", "地级市"},
    {"漳州", "zhangzhou", 24.5110, 117.6710, "福建", "地级市"},
    {"南平", "nanping", 26.6610, 118.1710, "福建", "地级市"},
    {"龙岩", "longyan", 25.1110, 117.0510, "福建", "地级市"},
    {"宁德", "ningde", 26.6510, 119.5110, "福建", "地级市"},
    
    {"南昌", "nanchang", 28.6810, 115.8510, "江西", "地级市"},
    {"景德镇", "jingdezhen", 29.3010, 117.2210, "江西", "地级市"},
    {"萍乡", "pingxiang", 27.6010, 113.8210, "江西", "地级市"},
    {"九江", "jiujiang", 29.7110, 115.9710, "江西", "地级市"},
    {"新余", "xinyu", 27.8210, 114.9210, "江西", "地级市"},
    {"鹰潭", "yingtan", 28.2310, 117.0710, "江西", "地级市"},
    {"赣州", "ganzhou", 25.8310, 114.9210, "江西", "地级市"},
    {"吉安", "jian", 27.0710, 114.9810, "江西", "地级市"},
    {"宜春", "yichun", 27.8110, 114.3710, "江西", "地级市"},
    {"抚州", "fuzhou", 27.9410, 116.3410, "江西", "地级市"},
    {"上饶", "shangrao", 28.4410, 117.9110, "江西", "地级市"},
    
    {"济南", "jinan", 36.6510, 117.1210, "山东", "地级市"},
    {"青岛", "qingdao", 36.0610, 120.3810, "山东", "地级市"},
    {"淄博", "zibo", 36.8010, 117.8510, "山东", "地级市"},
    {"枣庄", "zaozhuang", 34.8010, 117.5710, "山东", "地级市"},
    {"东营", "dongying", 37.4610, 118.4910, "山东", "地级市"},
    {"烟台", "yantai", 37.4610, 121.4410, "山东", "地级市"},
    {"潍坊", "weifang", 36.7110, 119.1610, "山东", "地级市"},
    {"济宁", "jining", 35.4210, 116.5710, "山东", "地级市"},
    {"泰安", "taian", 36.1910, 117.1210, "山东", "地级市"},
    {"威海", "weihai", 37.5010, 122.1610, "山东", "地级市"},
    {"日照", "rizhao", 35.4210, 119.4610, "山东", "地级市"},
    {"临沂", "linyi", 35.0510, 118.3510, "山东", "地级市"},
    {"德州", "dezhou", 37.4510, 116.2810, "山东", "地级市"},
    {"聊城", "liaocheng", 36.4510, 115.9710, "山东", "地级市"},
    {"滨州", "binzhou", 37.3610, 118.0210, "山东", "地级市"},
    {"菏泽", "heze", 35.2410, 115.4310, "山东", "地级市"},
    
    {"长沙", "changsha", 28.2210, 112.9410, "湖南", "地级市"},
    {"株洲", "zhuzhou", 27.8210, 113.1610, "湖南", "地级市"},
    {"湘潭", "xiangtan", 27.8710, 112.9110, "湖南", "地级市"},
    {"衡阳", "hengyang", 26.8910, 112.5910, "湖南", "地级市"},
    {"邵阳", "shaoyang", 27.2210, 111.4610, "湖南", "地级市"},
    {"岳阳", "yueyang", 29.3610, 113.1210, "湖南", "地级市"},
    {"常德", "changde", 29.0210, 111.6910, "湖南", "地级市"},
    {"张家界", "zhangjiajie", 29.1110, 110.4710, "湖南", "地级市"},
    {"益阳", "yiyang", 28.5610, 112.3110, "湖南", "地级市"},
    {"郴州", "chenzhou", 25.7910, 113.0210, "湖南", "地级市"},
    {"永州", "yongzhou", 26.2210, 111.6210, "湖南", "地级市"},
    {"怀化", "huaihua", 27.5310, 109.9710, "湖南", "地级市"},
    {"娄底", "loudi", 27.7210, 111.9410, "湖南", "地级市"},
    
    {"南宁", "nanning", 22.8110, 108.3610, "广西", "地级市"},
    {"柳州", "liuzhou", 24.3110, 109.4110, "广西", "地级市"},
    {"桂林", "guilin", 25.2810, 110.2910, "广西", "地级市"},
    {"梧州", "wuzhou", 23.4810, 111.2910, "广西", "地级市"},
    {"北海", "beihai", 21.4910, 109.1210, "广西", "地级市"},
    {"防城港", "fangchenggang", 21.6110, 108.3610, "广西", "地级市"},
    {"钦州", "qinzhou", 21.9610, 108.6710, "广西", "地级市"},
    {"贵港", "guigang", 23.1110, 109.6110, "广西", "地级市"},
    {"玉林", "yulin", 22.6410, 110.1510, "广西", "地级市"},
    {"百色", "baise", 23.8910, 106.6110, "广西", "地级市"},
    {"贺州", "hezhou", 24.4810, 111.5710, "广西", "地级市"},
    {"河池", "hechi", 24.7110, 108.0510, "广西", "地级市"},
    {"来宾", "laibin", 23.6310, 109.2310, "广西", "地级市"},
    {"崇左", "chongzuo", 22.4310, 107.3610, "广西", "地级市"},
    
    {"海口", "haikou", 20.0410, 110.3510, "海南", "地级市"},
    {"三亚", "sanya", 18.2510, 109.5110, "海南", "地级市"},
    {"文昌", "wenchang", 19.6210, 110.7510, "海南", "地级市"},
    {"琼海", "qionghai", 19.2010, 110.4510, "海南", "地级市"},
    {"万宁", "wanning", 18.8010, 110.3910, "海南", "地级市"},
    {"东方", "dongfang", 19.0910, 108.6610, "海南", "地级市"},
    
    {"贵阳", "guiyang", 26.6410, 106.6310, "贵州", "地级市"},
    {"六盘水", "liupanshui", 26.5910, 104.8110, "贵州", "地级市"},
    {"遵义", "zunyi", 27.7210, 106.9210, "贵州", "地级市"},
    {"安顺", "anshun", 26.2510, 105.9110, "贵州", "地级市"},
    {"毕节", "bijie", 27.1810, 105.2910, "贵州", "地级市"},
    {"铜仁", "tongren", 27.7210, 109.1410, "贵州", "地级市"},
    {"黔西南", "qianxinan", 25.0410, 104.9210, "贵州", "自治州"},
    {"黔东南", "qiandongnan", 26.5910, 108.0110, "贵州", "自治州"},
    {"黔南", "qiannan", 26.2110, 107.5110, "贵州", "自治州"},
    
    {"昆明", "kunming", 24.8910, 102.8310, "云南", "地级市"},
    {"曲靖", "qujing", 25.4910, 103.8210, "云南", "地级市"},
    {"玉溪", "yuxi", 24.3210, 102.5510, "云南", "地级市"},
    {"保山", "baoshan", 25.1110, 99.1910, "云南", "地级市"},
    {"昭通", "zhaotong", 27.3410, 103.7310, "云南", "地级市"},
    {"丽江", "lijiang", 26.8610, 100.2310, "云南", "地级市"},
    {"普洱", "pu'er", 22.7910, 100.9710, "云南", "地级市"},
    {"临沧", "lincang", 23.8810, 100.1710, "云南", "地级市"},
    
    {"拉萨", "lhasa", 29.6510, 91.1710, "西藏", "地级市"},
    
    {"兰州", "lanzhou", 36.0610, 103.8310, "甘肃", "地级市"},
    {"嘉峪关", "jiayuguan", 39.8110, 98.2810, "甘肃", "地级市"},
    {"金昌", "jinchang", 38.5010, 102.1810, "甘肃", "地级市"},
    {"白银", "baiyin", 36.5410, 104.1810, "甘肃", "地级市"},
    {"天水", "tianshui", 34.5610, 105.7210, "甘肃", "地级市"},
    {"酒泉", "jiuquan", 39.7310, 98.5110, "甘肃", "地级市"},
    {"张掖", "zhangye", 38.9310, 100.4610, "甘肃", "地级市"},
    {"武威", "wuwei", 37.9310, 102.6410, "甘肃", "地级市"},
    {"定西", "dingxi", 35.5610, 104.5310, "甘肃", "地级市"},
    {"陇南", "longnan", 33.3910, 104.9210, "甘肃", "地级市"},
    
    {"西宁", "xining", 36.6210, 101.7710, "青海", "地级市"},
    
    {"银川", "yinchuan", 38.4810, 106.2310, "宁夏", "地级市"},
    {"石嘴山", "shizuishan", 39.0510, 106.3910, "宁夏", "地级市"},
    {"吴忠", "wuzhong", 37.9910, 106.1910, "宁夏", "地级市"},
    {"固原", "guyuan", 36.0110, 106.2010, "宁夏", "地级市"},
    {"中卫", "zhongwei", 37.5310, 105.1810, "宁夏", "地级市"},
    
    {"乌鲁木齐", "wulumuqi", 43.8210, 87.6110, "新疆", "地级市"},
    {"克拉玛依", "kelamayi", 45.5810, 84.8210, "新疆", "地级市"},
    {"吐鲁番", "tulufan", 42.9110, 89.1810, "新疆", "地级市"},
    {"哈密", "hami", 42.8210, 93.5110, "新疆", "地级市"},
    
    {"呼和浩特", "huhehaote", 40.8210, 111.7510, "内蒙古", "地级市"},
    {"包头", "baotou", 40.6610, 109.8310, "内蒙古", "地级市"},
    {"乌海", "wuhai", 39.6510, 106.8210, "内蒙古", "地级市"},
    {"赤峰", "chifeng", 42.2810, 118.9110, "内蒙古", "地级市"},
    {"通辽", "tongliao", 43.6210, 122.2710, "内蒙古", "地级市"},
    {"鄂尔多斯", "eerduosi", 39.8010, 109.7810, "内蒙古", "地级市"},
    
    {"石家庄", "shijiazhuang", 38.0410, 114.5210, "河北", "地级市"},
    {"唐山", "tangshan", 39.6310, 118.1810, "河北", "地级市"},
    {"秦皇岛", "qinhuangdao", 39.9510, 119.5810, "河北", "地级市"},
    {"邯郸", "handan", 36.6010, 114.4810, "河北", "地级市"},
    {"邢台", "xingtai", 37.0610, 114.5110, "河北", "地级市"},
    {"保定", "baoding", 38.8710, 115.4810, "河北", "地级市"},
    {"张家口", "zhangjiakou", 40.7610, 114.8810, "河北", "地级市"},
    {"承德", "chengde", 40.9710, 117.9310, "河北", "地级市"},
    {"沧州", "cangzhou", 38.3010, 116.8310, "河北", "地级市"},
    {"廊坊", "langfang", 39.5310, 116.7010, "河北", "地级市"},
    {"衡水", "hengshui", 37.7210, 115.7110, "河北", "地级市"},
    
    {"太原", "taiyuan", 37.8710, 112.5510, "山西", "地级市"},
    {"大同", "datong", 40.0610, 113.3110, "山西", "地级市"},
    {"阳泉", "yangquan", 37.8610, 113.6010, "山西", "地级市"},
    {"长治", "changzhi", 36.1810, 113.0810, "山西", "地级市"},
    {"晋城", "jincheng", 35.5110, 112.8410, "山西", "地级市"},
    {"朔州", "shuozhou", 39.3210, 112.4410, "山西", "地级市"},
    {"晋中", "jinzhong", 37.6810, 112.7210, "山西", "地级市"},
    {"运城", "yuncheng", 35.0210, 110.9510, "山西", "地级市"},
    {"忻州", "xinzhou", 38.4210, 112.7310, "山西", "地级市"},
    {"临汾", "linfen", 36.0610, 111.5110, "山西", "地级市"},
    {"吕梁", "lvliang", 37.5010, 111.1110, "山西", "地级市"},
    
    {"沈阳", "shenyang", 41.8010, 123.4310, "辽宁", "地级市"},
    {"大连", "dalian", 38.9110, 121.6110, "辽宁", "地级市"},
    {"鞍山", "anshan", 41.1210, 122.8510, "辽宁", "地级市"},
    {"抚顺", "fushun", 41.8210, 123.9510, "辽宁", "地级市"},
    {"本溪", "benxi", 41.3210, 123.7510, "辽宁", "地级市"},
    {"丹东", "dandong", 40.1310, 124.3810, "辽宁", "地级市"},
    {"锦州", "jinzhou", 41.1210, 121.1510, "辽宁", "地级市"},
    {"营口", "yingkou", 40.6710, 122.2310, "辽宁", "地级市"},
    {"阜新", "fuxin", 42.0210, 121.6210, "辽宁", "地级市"},
    {"辽阳", "liaoyang", 41.2710, 123.1810, "辽宁", "地级市"},
    {"盘锦", "panjin", 41.1210, 122.0610, "辽宁", "地级市"},
    {"铁岭", "tieling", 42.2910, 123.8310, "辽宁", "地级市"},
    {"朝阳", "chaoyang", 41.5710, 120.4210, "辽宁", "地级市"},
    {"葫芦岛", "huludao", 40.7410, 120.8310, "辽宁", "地级市"},
    
    {"长春", "changchun", 43.8210, 125.3210, "吉林", "地级市"},
    {"吉林", "jilin", 43.8410, 126.5710, "吉林", "地级市"},
    {"四平", "siping", 43.1710, 124.3710, "吉林", "地级市"},
    {"辽源", "liaoyuan", 42.8210, 125.1410, "吉林", "地级市"},
    {"通化", "tonghua", 41.6210, 125.9210, "吉林", "地级市"},
    {"白山", "baishan", 41.9210, 126.5810, "吉林", "地级市"},
    {"松原", "songyuan", 45.1710, 124.8210, "吉林", "地级市"},
    {"白城", "baicheng", 45.6210, 122.8210, "吉林", "地级市"},
    
    {"哈尔滨", "haerbin", 45.8010, 126.5310, "黑龙江", "地级市"},
    {"齐齐哈尔", "qiqihaer", 47.3210, 123.9210, "黑龙江", "地级市"},
    {"鸡西", "jixi", 45.3210, 130.9710, "黑龙江", "地级市"},
    {"鹤岗", "hegang", 47.3510, 130.2910, "黑龙江", "地级市"},
    {"双鸭山", "shuangyashan", 46.6510, 131.1710, "黑龙江", "地级市"},
    {"大庆", "daqing", 46.5810, 125.0210, "黑龙江", "地级市"},
    {"伊春", "yichun", 47.7210, 128.9210, "黑龙江", "地级市"},
    {"佳木斯", "jiamusi", 46.8210, 130.3510, "黑龙江", "地级市"},
    {"七台河", "qitaihe", 45.7210, 131.0010, "黑龙江", "地级市"},
    {"牡丹江", "mudanjiang", 44.5810, 129.6210, "黑龙江", "地级市"},
    {"黑河", "heihe", 50.2210, 127.5310, "黑龙江", "地级市"},
    {"绥化", "suihua", 46.6310, 126.9910, "黑龙江", "地级市"},
    
    {"贵阳", "guiyang", 26.6410, 106.6310, "贵州", "地级市"},
    {"昆明", "kunming", 24.8910, 102.8310, "云南", "地级市"},
    {"拉萨", "lhasa", 29.6510, 91.1710, "西藏", "地级市"},
    {"西安", "xian", 34.3416, 108.9398, "陕西", "地级市"},
    {"兰州", "lanzhou", 36.0610, 103.8310, "甘肃", "地级市"},
    {"西宁", "xining", 36.6210, 101.7710, "青海", "地级市"},
    {"银川", "yinchuan", 38.4810, 106.2310, "宁夏", "地级市"},
    {"乌鲁木齐", "wulumuqi", 43.8210, 87.6110, "新疆", "地级市"},
    
    {"香港", "hongkong", 22.3193, 114.1694, "香港", "特别行政区"},
    {"澳门", "macau", 22.1987, 113.5439, "澳门", "特别行政区"}
};

const std::string API_KEY = "c22b01d72bbe469cb77699532913ff67";
const int DAILY_LIMIT = 1000;
const std::string COUNT_FILE = "weather_api_count.txt";

int getTodayCount()
{
    std::ifstream file(COUNT_FILE);
    if (!file.is_open())
        return 0;
    
    std::string date;
    int count;
    file >> date >> count;
    
    time_t now = time(nullptr);
    struct tm* t = localtime(&now);
    char today[11];
    strftime(today, sizeof(today), "%Y-%m-%d", t);
    
    if (date != today)
        return 0;
    
    return count;
}

void incrementCount()
{
    time_t now = time(nullptr);
    struct tm* t = localtime(&now);
    char today[11];
    strftime(today, sizeof(today), "%Y-%m-%d", t);
    
    int count = getTodayCount();
    count++;
    
    std::ofstream file(COUNT_FILE);
    if (file.is_open())
    {
        file << today << " " << count;
    }
}

bool checkDailyLimit()
{
    int count = getTodayCount();
    return count < DAILY_LIMIT;
}

std::string urlEncode(const std::string& str)
{
    std::string encoded;
    for (char c : str)
    {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_' || c == '.' || c == '~')
        {
            encoded += c;
        }
        else
        {
            char buf[4];
            sprintf(buf, "%%%02X", (unsigned char)c);
            encoded += buf;
        }
    }
    return encoded;
}

std::string decompressGzip(const std::string& compressed)
{
    std::string decompressed;
    z_stream strm;
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;
    strm.opaque = Z_NULL;
    strm.avail_in = (uInt)compressed.size();
    strm.next_in = (Bytef*)compressed.data();
    
    if (inflateInit2(&strm, 16 + MAX_WBITS) != Z_OK)
        return "";
    
    char buffer[4096];
    int ret;
    do {
        strm.avail_out = sizeof(buffer);
        strm.next_out = (Bytef*)buffer;
        ret = inflate(&strm, Z_NO_FLUSH);
        if (ret == Z_NEED_DICT || ret == Z_DATA_ERROR || ret == Z_MEM_ERROR)
        {
            inflateEnd(&strm);
            return "";
        }
        decompressed.append(buffer, sizeof(buffer) - strm.avail_out);
    } while (ret != Z_STREAM_END);
    
    inflateEnd(&strm);
    return decompressed;
}

std::string httpGet(const std::string& host, const std::string& path)
{
    HINTERNET hInternet = InternetOpenA("WeatherApp", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
    if (!hInternet)
        return "";

    HINTERNET hConnect = InternetConnectA(hInternet, host.c_str(), INTERNET_DEFAULT_HTTPS_PORT, NULL, NULL, INTERNET_SERVICE_HTTP, 0, 0);
    if (!hConnect)
    {
        InternetCloseHandle(hInternet);
        return "";
    }

    DWORD flags = INTERNET_FLAG_SECURE | INTERNET_FLAG_IGNORE_CERT_DATE_INVALID | INTERNET_FLAG_IGNORE_CERT_CN_INVALID;
    HINTERNET hRequest = HttpOpenRequestA(hConnect, "GET", path.c_str(), NULL, NULL, NULL, flags, 0);
    if (!hRequest)
    {
        InternetCloseHandle(hConnect);
        InternetCloseHandle(hInternet);
        return "";
    }

    BOOL bSend = HttpSendRequestA(hRequest, NULL, 0, NULL, 0);
    if (!bSend)
    {
        InternetCloseHandle(hRequest);
        InternetCloseHandle(hConnect);
        InternetCloseHandle(hInternet);
        return "";
    }

    DWORD statusCode = 0;
    DWORD statusCodeSize = sizeof(statusCode);
    HttpQueryInfoA(hRequest, HTTP_QUERY_STATUS_CODE | HTTP_QUERY_FLAG_NUMBER, &statusCode, &statusCodeSize, NULL);

    char contentEncoding[64] = {0};
    DWORD contentEncodingSize = sizeof(contentEncoding);
    HttpQueryInfoA(hRequest, HTTP_QUERY_CONTENT_ENCODING, contentEncoding, &contentEncodingSize, NULL);
    
    std::string response;
    char buffer[4096];
    DWORD bytesRead;

    while (InternetReadFile(hRequest, buffer, sizeof(buffer), &bytesRead) && bytesRead > 0)
    {
        response.append(buffer, bytesRead);
    }

    InternetCloseHandle(hRequest);
    InternetCloseHandle(hConnect);
    InternetCloseHandle(hInternet);

    if (strcmp(contentEncoding, "gzip") == 0 || strcmp(contentEncoding, "deflate") == 0)
    {
        return decompressGzip(response);
    }

    return response;
}

struct WeatherData
{
    std::string location;
    double temperature;
    double feelsLike;
    int humidity;
    double windSpeed;
    std::string condition;
    double visibility;
    double pressure;
    std::string sunrise;
    std::string sunset;
};

struct ForecastDay
{
    std::string date;
    double tempMin;
    double tempMax;
    std::string condition;
    std::string sunrise;
    std::string sunset;
    double precipitation;
};

struct WeatherWarning
{
    std::string typeName;
    std::string level;
    std::string title;
    std::string text;
    std::string startTime;
    std::string endTime;
};

WeatherData parseCurrentWeather(const json& data, const std::string& city)
{
    WeatherData wd;
    wd.location = city;
    
    auto& now = data["now"];
    wd.temperature = std::stod(now["temp"].get<std::string>());
    wd.feelsLike = std::stod(now["feelsLike"].get<std::string>());
    wd.humidity = std::stoi(now["humidity"].get<std::string>());
    wd.windSpeed = std::stod(now["windScale"].get<std::string>());
    wd.condition = now["text"].get<std::string>();
    
    wd.visibility = now.contains("vis") ? std::stod(now["vis"].get<std::string>()) : -1;
    wd.pressure = now.contains("pressure") ? std::stod(now["pressure"].get<std::string>()) : -1;

    wd.sunrise = "N/A";
    wd.sunset = "N/A";

    return wd;
}

std::vector<ForecastDay> parseForecast(const json& data)
{
    std::vector<ForecastDay> forecast;
    auto& daily = data["daily"];
    
    for (size_t i = 0; i < daily.size(); i++)
    {
        const json& item = daily[i];
        ForecastDay fd;
        fd.date = item["fxDate"].get<std::string>();
        fd.tempMax = std::stod(item["tempMax"].get<std::string>());
        fd.tempMin = std::stod(item["tempMin"].get<std::string>());
        fd.condition = item["textDay"].get<std::string>();
        fd.sunrise = item.contains("sunrise") ? item["sunrise"].get<std::string>() : "N/A";
        fd.sunset = item.contains("sunset") ? item["sunset"].get<std::string>() : "N/A";
        fd.precipitation = item.contains("precip") ? std::stod(item["precip"].get<std::string>()) : 0;
            
        forecast.push_back(fd);
    }
    return forecast;
}

std::vector<WeatherWarning> parseWarnings(const json& data)
{
    std::vector<WeatherWarning> warnings;
    if (!data.contains("warning") || data["warning"].empty())
        return warnings;
    
    auto& warningArray = data["warning"];
    for (size_t i = 0; i < warningArray.size(); i++)
    {
        const json& item = warningArray[i];
        WeatherWarning ww;
        ww.typeName = item.contains("typeName") ? item["typeName"].get<std::string>() : "";
        ww.level = item.contains("level") ? item["level"].get<std::string>() : "";
        ww.title = item.contains("title") ? item["title"].get<std::string>() : "";
        ww.text = item.contains("text") ? item["text"].get<std::string>() : "";
        ww.startTime = item.contains("startTime") ? item["startTime"].get<std::string>() : "";
        ww.endTime = item.contains("endTime") ? item["endTime"].get<std::string>() : "";
        warnings.push_back(ww);
    }
    return warnings;
}

void printWeather(const WeatherData& wd)
{
    std::cout << "\n";
    std::cout << "========================================\n";
    std::cout << "           实时天气预报\n";
    std::cout << "========================================\n";
    std::cout << std::setw(20) << std::left << "城市:" << wd.location << "\n";
    std::cout << std::setw(20) << std::left << "温度:" << std::fixed << std::setprecision(1) << wd.temperature << "°C\n";
    std::cout << std::setw(20) << std::left << "体感温度:" << std::fixed << std::setprecision(1) << wd.feelsLike << "°C\n";
    std::cout << std::setw(20) << std::left << "天气状况:" << wd.condition << "\n";
    std::cout << std::setw(20) << std::left << "湿度:" << wd.humidity << "%\n";
    std::cout << std::setw(20) << std::left << "风速:" << wd.windSpeed << " 级\n";
    if (wd.pressure >= 0)
        std::cout << std::setw(20) << std::left << "气压:" << std::fixed << std::setprecision(1) << wd.pressure << " hPa\n";
    if (wd.visibility >= 0)
        std::cout << std::setw(20) << std::left << "能见度:" << std::fixed << std::setprecision(1) << wd.visibility << " km\n";
    std::cout << std::setw(20) << std::left << "日出:" << wd.sunrise << "\n";
    std::cout << std::setw(20) << std::left << "日落:" << wd.sunset << "\n";
    std::cout << "========================================\n";
}

void printWeatherIcon(const std::string& condition)
{
    std::cout << "\n";
    std::cout << "           ";
    if (condition.find("晴") != std::string::npos)
    {
        std::cout << "   ☀️   \n";
        std::cout << "   ☀☀☀   \n";
        std::cout << "  ☀☀☀☀☀  \n";
    }
    else if (condition.find("多云") != std::string::npos)
    {
        std::cout << " ☁️☁️☁️  \n";
        std::cout << "☁️☁️☁️☁️☁️ \n";
        std::cout << " ☁️☁️☁️  \n";
    }
    else if (condition.find("阴") != std::string::npos)
    {
        std::cout << "☁️☁️☁️☁️☁️\n";
        std::cout << "☁️☁️☁️☁️☁️\n";
        std::cout << "☁️☁️☁️☁️☁️\n";
    }
    else if (condition.find("雨") != std::string::npos)
    {
        std::cout << " ☁️☁️☁️  \n";
        std::cout << "  💧💧💧  \n";
        std::cout << " 💧💧💧💧 \n";
    }
    else if (condition.find("雷") != std::string::npos)
    {
        std::cout << " ☁️☁️☁️  \n";
        std::cout << "  ⚡⚡⚡  \n";
        std::cout << " ☁️☁️☁️  \n";
    }
    else if (condition.find("雪") != std::string::npos)
    {
        std::cout << " ❄️❄️❄️ \n";
        std::cout << "❄️❄️❄️❄️❄️\n";
        std::cout << " ❄️❄️❄️ \n";
    }
    else if (condition.find("雾") != std::string::npos)
    {
        std::cout << " 🌫️🌫️🌫️ \n";
        std::cout << "🌫️🌫️🌫️🌫️🌫️\n";
        std::cout << " 🌫️🌫️🌫️ \n";
    }
    else
    {
        std::cout << "   ☀️   \n";
        std::cout << "   ☀☀☀   \n";
        std::cout << "  ☀☀☀☀☀  \n";
    }
    std::cout << "\n";
}

void printForecast(const std::vector<ForecastDay>& forecast)
{
    std::cout << "\n";
    std::cout << "========================================\n";
    std::cout << "           未来7天天气预报\n";
    std::cout << "========================================\n";
    std::cout << std::setw(12) << std::left << "日期" 
              << std::setw(12) << std::left << "最高温" 
              << std::setw(12) << std::left << "最低温" 
              << std::setw(12) << std::left << "天气状况"
              << std::setw(12) << std::left << "降水量(mm)\n";
    std::cout << "----------------------------------------\n";
    
    for (const auto& day : forecast)
    {
        std::string maxStr = std::to_string((int)(day.tempMax * 10 + 0.5) / 10.0);
        std::string minStr = std::to_string((int)(day.tempMin * 10 + 0.5) / 10.0);
        std::string precipStr = std::to_string((int)(day.precipitation * 10 + 0.5) / 10.0);
        
        size_t maxDot = maxStr.find('.');
        if (maxDot != std::string::npos && maxStr.size() > maxDot + 2)
            maxStr = maxStr.substr(0, maxDot + 2);
        size_t minDot = minStr.find('.');
        if (minDot != std::string::npos && minStr.size() > minDot + 2)
            minStr = minStr.substr(0, minDot + 2);
        size_t precipDot = precipStr.find('.');
        if (precipDot != std::string::npos && precipStr.size() > precipDot + 2)
            precipStr = precipStr.substr(0, precipDot + 2);
        
        std::cout << std::setw(12) << std::left << day.date.substr(5);
        std::cout << std::setw(12) << std::left << maxStr + "°C";
        std::cout << std::setw(12) << std::left << minStr + "°C";
        std::cout << std::setw(12) << std::left << day.condition;
        std::cout << std::setw(12) << std::left << precipStr << "\n";
    }
    std::cout << "========================================\n";
}

void printPrecipitationChart(const std::vector<ForecastDay>& forecast)
{
    std::cout << "\n";
    std::cout << "========================================\n";
    std::cout << "           降水趋势图\n";
    std::cout << "========================================\n";
    
    if (forecast.empty())
    {
        std::cout << "暂无降水数据\n";
        std::cout << "========================================\n";
        return;
    }
    
    double maxPrecip = 0;
    for (const auto& day : forecast)
    {
        if (day.precipitation > maxPrecip)
            maxPrecip = day.precipitation;
    }
    
    if (maxPrecip == 0)
        maxPrecip = 1;
    
    const int chartHeight = 10;
    
    for (int row = chartHeight; row >= 0; row--)
    {
        double threshold = (maxPrecip / chartHeight) * row;
        
        if (row == chartHeight)
            std::cout << std::setw(8) << std::right << std::fixed << std::setprecision(1) << maxPrecip << "mm |";
        else if (row == 0)
            std::cout << std::setw(8) << std::right << "0.0mm |";
        else
            std::cout << std::setw(8) << std::right << "      |";
        
        for (const auto& day : forecast)
        {
            if (day.precipitation >= threshold && threshold > 0)
                std::cout << " ██";
            else if (day.precipitation > 0 && row == 0)
                std::cout << " ░░";
            else
                std::cout << "  ";
        }
        std::cout << "\n";
    }
    
    std::cout << "        +";
    for (size_t i = 0; i < forecast.size(); i++)
        std::cout << "--";
    std::cout << "\n        ";
    
    for (const auto& day : forecast)
    {
        std::string date = day.date.substr(5);
        if (date.size() >= 5)
            std::cout << date.substr(3);
        else
            std::cout << date;
    }
    std::cout << "\n";
    std::cout << "========================================\n";
}

void printWarnings(const std::vector<WeatherWarning>& warnings)
{
    if (warnings.empty())
        return;
    
    std::cout << "\n";
    std::cout << "========================================\n";
    std::cout << "           天气预警信息\n";
    std::cout << "========================================\n";
    
    for (const auto& warning : warnings)
    {
        std::cout << "\n【" << warning.typeName << "】" << warning.level << "\n";
        std::cout << "标题: " << warning.title << "\n";
        std::cout << "内容: " << warning.text << "\n";
        if (!warning.startTime.empty())
            std::cout << "生效时间: " << warning.startTime;
        if (!warning.endTime.empty())
            std::cout << " ~ " << warning.endTime << "\n";
        else if (!warning.startTime.empty())
            std::cout << "\n";
        std::cout << "----------------------------------------\n";
    }
    std::cout << "========================================\n";
}

void printHenanCities()
{
    std::cout << "\n========================================\n";
    std::cout << "           河南省城市列表\n";
    std::cout << "========================================\n";
    std::cout << "\n【地级市】\n";
    
    for (const auto& city : cityDatabase)
    {
        if (city.province == "河南" && city.type == "地级市")
        {
            std::cout << "  " << city.name << " (" << city.pinyin << ")\n";
        }
    }
    
    std::cout << "\n【主要区县】\n";
    for (const auto& city : cityDatabase)
    {
        if (city.province == "河南" && city.type == "区县")
        {
            std::cout << "  " << city.name << "\n";
        }
    }
    std::cout << "\n========================================\n";
}

CityInfo findCity(const std::string& name)
{
    for (const auto& city : cityDatabase)
    {
        if (city.name == name || city.pinyin == name)
        {
            return city;
        }
    }
    return {"", "", 39.9042, 116.4074, "", ""};
}

CityInfo findCityByName(const std::string& cityName)
{
    for (const auto& city : cityDatabase)
    {
        if (city.name == cityName || city.pinyin == cityName)
            return city;
    }
    
    std::string simplified = cityName;
    size_t pos = simplified.find("市");
    if (pos != std::string::npos)
        simplified = simplified.substr(0, pos);
    
    for (const auto& city : cityDatabase)
    {
        if (city.name == simplified)
            return city;
    }
    
    return {"", "", 39.9042, 116.4074, "", ""};
}

bool queryWeatherByLocation(const std::string& location, const std::string& cityName)
{
    if (!checkDailyLimit())
    {
        std::cout << "\n今日API调用次数已达上限（1000次），请明日再试！\n";
        return false;
    }

    std::cout << "\n正在获取实时天气数据...\n";
    incrementCount();
    
    std::string nowPath = "/v7/weather/now?location=" + location + "&key=" + API_KEY;
    std::string nowResponse = httpGet("nr7qquf7hj.re.qweatherapi.com", nowPath);
    
    if (nowResponse.empty())
    {
        std::cout << "获取实时天气数据失败，请检查网络连接！\n";
        return false;
    }

    std::cout << "正在获取天气预报数据...\n";
    incrementCount();
    
    std::string forecastPath = "/v7/weather/7d?location=" + location + "&key=" + API_KEY;
    std::string forecastResponse = httpGet("nr7qquf7hj.re.qweatherapi.com", forecastPath);
    
    if (forecastResponse.empty())
    {
        std::cout << "获取天气预报数据失败，请检查网络连接！\n";
        return false;
    }

    std::cout << "正在获取天气预警数据...\n";
    incrementCount();
    
    std::string warningPath = "/v7/warning/now?location=" + location + "&key=" + API_KEY;
    std::string warningResponse = httpGet("nr7qquf7hj.re.qweatherapi.com", warningPath);

    try
    {
        json forecastData = json::parse(forecastResponse);
        if (forecastData["code"].get<std::string>() != "200")
        {
            std::cout << "错误：该位置天气数据不可用，和风天气免费版仅支持部分地区数据。\n";
            std::cout << "请尝试查询中国境内城市或其他支持的地区。\n";
            return false;
        }
        
        std::vector<ForecastDay> forecast = parseForecast(forecastData);
        
        json nowData = json::parse(nowResponse);
        if (nowData["code"].get<std::string>() != "200")
        {
            std::cout << "错误：该位置天气数据不可用，和风天气免费版仅支持部分地区数据。\n";
            std::cout << "请尝试查询中国境内城市或其他支持的地区。\n";
            return false;
        }
        
        WeatherData wd = parseCurrentWeather(nowData, cityName);
        
        if (!forecast.empty())
        {
            time_t now = time(nullptr);
            struct tm* t = localtime(&now);
            char today[11];
            strftime(today, sizeof(today), "%Y-%m-%d", t);
            
            for (const auto& day : forecast)
            {
                if (day.date == today)
                {
                    wd.sunrise = day.sunrise;
                    wd.sunset = day.sunset;
                    break;
                }
            }
        }
        
        printWeather(wd);
        printForecast(forecast);
        printPrecipitationChart(forecast);
        
        if (!warningResponse.empty())
        {
            json warningData = json::parse(warningResponse);
            std::vector<WeatherWarning> warnings = parseWarnings(warningData);
            printWarnings(warnings);
        }
    }
    catch (const std::exception& e)
    {
        std::cout << "解析天气数据失败: " << e.what() << "\n";
        return false;
    }

    return true;
}

int main(int argc, char* argv[])
{
    system("chcp 65001 >nul");
    
    while (true)
    {
        system("cls");
        
        std::cout << "========================================\n";
        std::cout << "         Weather_API v0.0.6\n";
        std::cout << "========================================\n";
        std::cout << "\n请选择查询方式:\n";
        std::cout << "  1. 输入城市名称查询（支持全国城市）\n";
        std::cout << "  2. 输入经纬度查询（支持全球天气）\n";
        std::cout << "  3. 查看河南省城市列表\n";
        std::cout << "  0. 退出程序\n";
        std::cout << "\n请输入选择: ";
        
        int choice;
        while (!(std::cin >> choice))
        {
            std::cin.clear();
            std::cin.ignore(1000, '\n');
            std::cout << "\n错误：请输入有效的数字！\n";
            std::cout << "请输入选择: ";
        }
        std::cin.ignore();
        
        if (choice < 0 || choice > 3)
        {
            std::cout << "\n错误：请输入 0-3 之间的数字！\n";
            system("pause");
            continue;
        }
        
        if (choice == 0)
        {
            std::cout << "感谢使用！\n";
            break;
        }
        else if (choice == 1)
        {
            std::cout << "\n请输入城市名称: ";
            std::string city;
            std::getline(std::cin, city);
            
            if (city.empty())
            {
                std::cout << "\n错误：城市名称不能为空！\n";
                system("pause");
                continue;
            }
            
            CityInfo cityInfo = findCityByName(city);
            
            if (cityInfo.name.empty())
            {
                std::cout << "\n错误：暂不支持该城市，请尝试输入经纬度查询，或查看支持的城市列表！\n";
                system("pause");
                continue;
            }
            
            std::string location = std::to_string(cityInfo.longitude) + "," + std::to_string(cityInfo.latitude);
            queryWeatherByLocation(location, cityInfo.name);
        }
        else if (choice == 2)
        {
            std::cout << "\n请输入经纬度（格式: 经度,纬度，例如: 116.4074,39.9042）: ";
            std::string location;
            std::getline(std::cin, location);
            
            if (location.empty())
            {
                std::cout << "\n错误：经纬度不能为空！\n";
                system("pause");
                continue;
            }
            
            size_t commaPos = location.find(',');
            if (commaPos == std::string::npos)
            {
                std::cout << "\n错误：格式错误，请输入: 经度,纬度\n";
                system("pause");
                continue;
            }
            
            std::string lonStr = location.substr(0, commaPos);
            std::string latStr = location.substr(commaPos + 1);
            
            try
            {
                double longitude = std::stod(lonStr);
                double latitude = std::stod(latStr);
                
                if (longitude < -180 || longitude > 180)
                {
                    std::cout << "\n错误：经度范围应为 -180 到 180！\n";
                    system("pause");
                    continue;
                }
                
                if (latitude < -90 || latitude > 90)
                {
                    std::cout << "\n错误：纬度范围应为 -90 到 90！\n";
                    system("pause");
                    continue;
                }
                
                queryWeatherByLocation(location, "自定义位置");
            }
            catch (...)
            {
                std::cout << "\n错误：经纬度格式不正确，请输入数字！\n";
                system("pause");
                continue;
            }
        }
        else if (choice == 3)
        {
            printHenanCities();
            std::cout << "\n按任意键返回...\n";
            system("pause >nul");
            continue;
        }
        
        int todayCount = getTodayCount();
        std::cout << "\n今日剩余API调用次数: " << (DAILY_LIMIT - todayCount) << "\n";
        
        std::cout << "\n按任意键继续查询...\n";
        system("pause >nul");
    }
    
    return 0;
}