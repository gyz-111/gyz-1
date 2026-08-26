# -*- coding: utf-8 -*-
import sys
import os

def add_city(city_name, pinyin, latitude, longitude, province, city_type):
    cpp_file = 'Weather.cpp'
    
    with open(cpp_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_city = ',\n    {"%s", "%s", %s, %s, "%s", "%s"}' % (city_name, pinyin, latitude, longitude, province, city_type)
    
    marker = '{"澳门", "macau", 22.1987, 113.5439, "澳门", "特别行政区"}'
    index = content.find(marker)
    if index == -1:
        print("错误：未找到澳门标记！")
        sys.exit(1)
    
    insert_pos = index + len(marker)
    content = content[:insert_pos] + new_city + content[insert_pos:]
    
    with open(cpp_file, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print("城市数据添加成功！")

if __name__ == '__main__':
    if len(sys.argv) != 7:
        print("用法: python add_city.py <城市名称> <拼音> <纬度> <经度> <省份> <类型>")
        sys.exit(1)
    
    city_name = sys.argv[1]
    pinyin = sys.argv[2]
    latitude = float(sys.argv[3])
    longitude = float(sys.argv[4])
    province = sys.argv[5]
    city_type = sys.argv[6]
    
    add_city(city_name, pinyin, latitude, longitude, province, city_type)