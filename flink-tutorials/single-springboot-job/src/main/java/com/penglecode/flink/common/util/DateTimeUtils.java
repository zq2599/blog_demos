package com.penglecode.flink.common.util;

import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于JDK8时间框架的日期时间处理工具类
 *
 * @author pengpeng
 * @version 1.0
 * @since 2021/5/15 14:02
 */
public class DateTimeUtils {

	/**
	 * 默认的日期格式: yyyy-MM-dd
	 */
	public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	
	/**
	 * 默认的日期格式: yyyy-MM-dd
	 */
	public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
	
	/**
	 * 默认的日期+时间格式: yyyy-MM-dd HH:mm:ss
	 */
	public static final String DEFAULT_DATETIME_PATTERN = DEFAULT_DATE_PATTERN + " " + DEFAULT_TIME_PATTERN;
	
	/**
	 * 默认带毫秒数的时间戳格式
	 */
	private static final Pattern TIMESTAMP_MSEC_REGEX_PATTERN = Pattern.compile("\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}");
	
	private DateTimeUtils() {}

	/**
	 * <p>将@{code java.util.Date}转换为@{code java.time.LocalDateTime}
	 * 
	 * @param date
	 * @return
	 */
	public static LocalDateTime from(Date date){
		checkDate(date);
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	/**
	 * <p>将@{code java.util.Date}转换为@{code java.time.LocalDateTime}
	 * 
	 * @param dateTime
	 * @return
	 */
	public static Date from(LocalDateTime dateTime){
		checkDateTime(dateTime);
		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	/**
	 * <p>将@{code java.time.LocalDateTime}以指定的日期格式格式化为字符串</p>
	 * 
	 * @param dateTime
	 * @param pattern
	 * @return
	 */
	public static String format(LocalDateTime dateTime, String pattern){
		checkDateTime(dateTime);
		checkPattern(pattern);
		return dateTime.format(DateTimeFormatter.ofPattern(pattern));
	}
	
	/**
	 * <p>将@{code java.util.Date}以指定的日期格式格式化为字符串</p>
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern){
		checkDate(date);
		checkPattern(pattern);
		return from(date).format(DateTimeFormatter.ofPattern(pattern));
	}
	
	/**
	 * <p>以指定的日期格式格式化当前时间</p>
	 * 
	 * @param pattern
	 * @return
	 */
	public static String formatNow(String pattern){
		checkPattern(pattern);
		return LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
	}
	
	/**
	 * <p>以默认的日期格式(yyyy-MM-dd HH:mm:ss)格式化当前时间</p>
	 * 
	 * @return
	 */
	public static String formatNow(){
		return formatNow(DEFAULT_DATETIME_PATTERN);
	}
	
	/**
	 * <p>将字符串格式的日期转换为@{java.time.LocalDateTime}</p>
	 * 
	 * @param dateTime		- 日期字符串形式的值
	 * @param pattern		- 针对dateTimeText的日期格式
	 * @return
	 */
	public static LocalDateTime parse2DateTime(String dateTime, String pattern){
		Assert.hasText(dateTime, "Parameter 'dateTime' can not be empty!");
		checkPattern(pattern);
		String format = pattern;
		String text = dateTime;
		Matcher matcher;
		String suffix = ".SSS";
		matcher = TIMESTAMP_MSEC_REGEX_PATTERN.matcher(dateTime);
		//dateTime以毫秒结尾 && 格式pattern中没有以.SSS结尾
		if(matcher.find() && matcher.end() == dateTime.length() && !pattern.endsWith(suffix)){
			format = format + suffix;
		//dateTimeText没有以毫秒结尾 && 格式pattern中以.SSS结尾
		}else if(matcher.find() && matcher.end() == dateTime.length() && pattern.endsWith(suffix)){
			text = text + ".0";
		}
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(format)
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
			    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
			    .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
			    .toFormatter();
		return LocalDateTime.parse(text, formatter);
	}
	
	/**
	 * <p>将字符串格式的日期转换为@{code java.util.Date}</p>
	 * 
	 * @param dateTimeText		- 日期字符串形式的值
	 * @param pattern			- 针对dateTimeText的日期格式
	 * @return
	 */
	public static Date parse2Date(String dateTimeText, String pattern){
		return from(parse2DateTime(dateTimeText, pattern));
	}
	
	/**
	 * 检测dateTime的日期格式是否是pattern
	 * @param dateTime
	 * @param pattern
	 * @return
	 */
	public static boolean matchesDatePattern(String dateTime, String pattern) {
		if(dateTime != null){
			try {
				parse2DateTime(dateTime, pattern);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * 标准化dateTimeText，将其他格式的日期格式统一标准化为yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
	 * @param dateTime
	 * @return
	 */
	public static String normalizeDateTime(String dateTime) {
		if(dateTime != null) {
			dateTime = dateTime.replace("T", " ");
			dateTime = dateTime.replace("/", "-");
			dateTime = dateTime.replace("年", "-");
			dateTime = dateTime.replace("月", "-");
			dateTime = dateTime.replace("日", "");
			dateTime = dateTime.replace("时", ":");
			dateTime = dateTime.replace("分", ":");
			dateTime = dateTime.replace("秒", "");
		}
		return dateTime;
	}
	
	/**
	 * 按标准日期时间格式来解析dateTimeText
	 * @param dateTimeText
	 * @return
	 */
	public static LocalDateTime parse2DateTime(String dateTimeText) {
		dateTimeText = DateTimeUtils.normalizeDateTime(dateTimeText);
		if(DateTimeUtils.matchesDatePattern(dateTimeText, DateTimeUtils.DEFAULT_DATE_PATTERN)) { //yyyy-MM-dd
			return parse2DateTime(dateTimeText, DateTimeUtils.DEFAULT_DATE_PATTERN);
		} else if (DateTimeUtils.matchesDatePattern(dateTimeText, DateTimeUtils.DEFAULT_DATETIME_PATTERN)) { //yyyy-MM-dd HH:mm:ss
			return parse2DateTime(dateTimeText, DateTimeUtils.DEFAULT_DATETIME_PATTERN);
		}
		return null;
	}
	
	/**
	 * 按标准日期时间格式来解析dateTimeText
	 * @param dateTimeText
	 * @return
	 */
	public static Date parse2Date(String dateTimeText) {
		dateTimeText = DateTimeUtils.normalizeDateTime(dateTimeText);
		if(DateTimeUtils.matchesDatePattern(dateTimeText, DateTimeUtils.DEFAULT_DATE_PATTERN)) { //yyyy-MM-dd
			return parse2Date(dateTimeText, DateTimeUtils.DEFAULT_DATE_PATTERN);
		} else if (DateTimeUtils.matchesDatePattern(dateTimeText, DateTimeUtils.DEFAULT_DATETIME_PATTERN)) { //yyyy-MM-dd HH:mm:ss
			return parse2Date(dateTimeText, DateTimeUtils.DEFAULT_DATETIME_PATTERN);
		}
		return null;
	}
	
	/**
	 * 毫秒时间戳转LocalDateTime
	 * @param timestamp
	 * @return
	 */
	public static LocalDateTime ofEpochMilli(long timestamp) {
	    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
	}
	
	/**
	 * LocalDateTime转毫秒时间戳
	 * @param dateTime
	 * @return
	 */
	public static Long toEpochMilli(LocalDateTime dateTime) {
		return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	private static void checkDate(Date date) {
		Assert.notNull(date, "Parameter 'date' can not be null!");
	}

	private static void checkDateTime(LocalDateTime dateTime) {
		Assert.notNull(dateTime, "Parameter 'dateTime' can not be null!");
	}

	private static void checkPattern(String pattern) {
		Assert.hasText(pattern, "Parameter 'pattern' can not be empty!");
	}

}