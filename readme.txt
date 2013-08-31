建立文件/application/search/logrotate/nginx
内容如下

/application/search/nginx/default/logs/logcenter.log {
nocompress
daily
missingok
copytruncate
create
notifempty
sharedscripts
postrotate
if [ -f /application/search/nginx/default/logs/nginx.pid ]; then
kill -USR1 `cat /application/search/nginx/default/logs/nginx.pid`
CURRENT_TIME=` date -d "-1 minute" +%Y%m%d%H%M`
mv /application/search/nginx/default/logs/logcenter.log.1 /application/search/nginx/default/logs/logcenter.${CURRENT_TIME}.log
fi
endscript
rotate 1
}


配置crontab
crontab -e
*/1 * * * * /usr/sbin/logrotate -f /application/search/logrotate/nginx

重启crontab服务




日志中心处理流程


1)输入示例(3行)
20/Oct/2012:16:08:51 +0800 "GET /__utm.gif?utmpid=&utmdvd=&utmtu=http%3A%2F%2Fopen.panguso.com%2Fwidget%2Fwap%2Fsmzy%2Fwap%2Fsearch%2Fweb%3Ffr%3Dwap1%26ptn%3D3325%26pmd%3Dwapps1%26q%3D%25E5%25A4%25A7%25E7%25B1%25B3&utmpmd=wapps1&utmts=20121020160851706&utmrfr=&utmptn=3325&utmenc=UTF-8&utmua=panguso_boce&utmmn=&utmac=a98d85590402440783574196e9c105ef&utmuid=&utmrip=10.10.128.100&utmoip=218.61.13.23 HTTP/1.0"
20/Oct/2012:16:09:13 +0800 "GET /__utm.gif?utmpid=&utmdvd=&utmtu=http%3A%2F%2Fopen.panguso.com%2Fwidget%2Fwap%2Fsmzy%2Fwap%2Fsearch%2Fweb%3Ffr%3Dwap2%26ptn%3D3325%26pmd%3Dwapps2%26q%3D%25E5%25A4%25A7%25E7%25B1%25B3&utmpmd=wapps2&utmts=20121020160913131&utmrfr=&utmptn=3325&utmenc=UTF-8&utmua=panguso_boce&utmmn=&utmac=a98d85590402440783574196e9c105ef&utmuid=&utmrip=10.10.128.52&utmoip=113.10.180.174 HTTP/1.0"
20/Oct/2012:16:09:16 +0800 "GET /__utm.gif?utmpid=&utmdvd=&utmtu=http%3A%2F%2Fopen.panguso.com%2Fwidget%2Fwap%2Fsmzy%2Fwap%2Fsearch%2Fweb%3Ffr%3Dwap1%26ptn%3D3325%26pmd%3Dwapps1%26q%3D%25E5%25A4%25A7%25E7%25B1%25B3&utmpmd=wapps1&utmts=20121020160916940&utmrfr=&utmptn=3325&utmenc=UTF-8&utmua=panguso_boce&utmmn=&utmac=a98d85590402440783574196e9c105ef&utmuid=&utmrip=10.10.128.52&utmoip=113.10.180.174 HTTP/1.0"

【main函数】
加载checkMap数据到memcached，存放utmac和utmlid，其中key为utmac，value为List<utmlid>
main结束的时候，清理memcached中的checkMap

【mapper】----数据清洗
从memcached加载checkMap到内存中
将URI的"?"部分之后的query参数字符串截取出来
判断日志是否合法，利用checkMap
合法的日志输出到Context，其中key为logid，value为query字符串


【combiner】
其实本质上就是在内存中运行直接读取数据运行reduce任务
根据logid加载日志格式metadata
开始解析日志并输出


【reducer】
本质上reduce是在硬盘上加载数据到内存中，在运行reduce任务
可以考虑禁用，用来提高性能
