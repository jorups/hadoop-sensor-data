select max(temperature),device_id from sensor_data group by device_id
select count(*),device_id from sensor_data group by device_id

select device_id, max(temperature) from sensor_data 
where to_date(from_unixtime(unix_timestamp(time))) = "2018-05-13"
group by device_id
