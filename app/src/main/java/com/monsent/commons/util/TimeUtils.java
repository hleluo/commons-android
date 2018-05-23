package com.monsent.commons.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

	public final static String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";
	public final static String yyyyMMddHHmmss = "yyyyMMddHHmmss";

	/**
	 * 字符串转时间对象
	 * @param dateStr 字符串
	 * @param format 格式
	 * @return 时间对象
	 */
	public static Date parseFormat(String dateStr, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 时间对象转字符串
	 * @param date 时间对象
	 * @param format 格式
	 * @return 字符串
	 */
	public static String dateFormat(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		return sdf.format(date);
	}

	/* -------- UTC Date -------- */

	/**
	 * 本地时间转UTC时间
	 * @param localDate 本地时间
	 * @return UTC时间
	 */
	public static Date getUtcDate(Date localDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(localDate);
		int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
		int dstOffset = calendar.get(Calendar.DST_OFFSET);
		calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return calendar.getTime();
	}

	/**
	 * 本地时间字符串转UTC时间
	 * @param localDateStr 本地时间字符串
	 * @param format 格式
	 * @return UTC时间
	 */
	public static Date getUtcDate(String localDateStr, String format) {
		return getUtcDate(parseFormat(localDateStr, format));
	}

	/**
	 * 获取当前UTC时间
	 * @return UTC时间
	 */
	public static Date getCurrentUtcDate() {
		return getUtcDate(new Date());
	}

	/* -------- UTC Date String -------- */

	/**
	 * 本地时间转UTC时间字符串
	 * @param localDate 本地时间
	 * @param format 格式
	 * @return UTC时间字符串
	 */
	public static String getUtcDateStr(Date localDate, String format) {
		return dateFormat(getUtcDate(localDate), format);
	}

	/**
	 * 本地时间字符串传UTC时间字符串
	 * @param localDateStr 本地时间字符串
	 * @param localFormat 本地格式
	 * @param utcFormat UTC格式
	 * @return UTC时间字符串
	 */
	public static String getUtcDateStr(String localDateStr, String localFormat, String utcFormat) {
		return getUtcDateStr(parseFormat(localDateStr, localFormat), utcFormat);
	}

	/**
	 * 本地时间字符串转UTC时间字符串
	 * @param localDateStr 本地时间字符串
	 * @param format 格式
	 * @return UTC时间字符串
	 */
	public static String getUtcDateStr(String localDateStr, String format) {
		return getUtcDateStr(localDateStr, format, format);
	}

	/**
	 * 获取当前UTC时间字符串
	 * @param format 格式
	 * @return UTC时间字符串
	 */
	public static String getCurrentUtcDateStr(String format) {
		return getUtcDateStr(new Date(), format);
	}

	/* -------- Local Date -------- */

	/**
	 * UTC时间转本地时间
	 * @param utcDate UTC时间
	 * @return 本地时间
	 */
	public static Date getLocalDate(Date utcDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(utcDate);
		int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
		int dstOffset = calendar.get(Calendar.DST_OFFSET);
		calendar.add(Calendar.MILLISECOND, zoneOffset + dstOffset);
		return calendar.getTime();
	}

	/**
	 * UTC时间字符串转本地时间
	 * @param utcDateStr UTC时间字符串
	 * @param format 格式
	 * @return 本地时间
	 */
	public static Date getLocalDate(String utcDateStr, String format) {
		return getLocalDate(parseFormat(utcDateStr, format));
	}

	/**
	 * 获取当前本地时间
	 * @return 本地时间
	 */
	public static Date getCurrentLocalDate() {
		return new Date();
	}
	
	/* -------- Local Date String -------- */

	/**
	 * UTC时间转本地时间字符串
	 * @param utcDate UTC时间
	 * @param format 格式
	 * @return 本地时间字符串
	 */
	public static String getLocalDateStr(Date utcDate, String format) {
		return dateFormat(getLocalDate(utcDate), format);
	}

	/**
	 * UTC时间字符串转本地时间字符串
	 * @param utcDateStr UTC时间字符串
	 * @param utcFormat UTC格式
	 * @param localFormat 本地格式
	 * @return 本地时间字符串
	 */
	public static String getLocalDateStr(String utcDateStr, String utcFormat, String localFormat) {
		return getLocalDateStr(parseFormat(utcDateStr, utcFormat), localFormat);
	}

	/**
	 * UTC时间字符串转本地时间字符串
	 * @param utcDateStr UTC时间字符串
	 * @param format 格式
	 * @return 本地时间字符串
	 */
	public static String getLocalDateStr(String utcDateStr, String format) {
		return getLocalDateStr(utcDateStr, format, format);
	}

	/**
	 * 获取当前本地时间字符串
	 * @param format 格式
	 * @return 本地时间字符串
	 */
	public static String getCurrentLocalDateStr(String format) {
		return dateFormat(new Date(), format);
	}

	/* -------- main -------- */
	public static void main(String[] args) {
		System.out.println(getCurrentUtcDateStr(yyyyMMddHHmmssSSS));
		System.out.println(getCurrentLocalDateStr(yyyyMMddHHmmssSSS));
	}

}
