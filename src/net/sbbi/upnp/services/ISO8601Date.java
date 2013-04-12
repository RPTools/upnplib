/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.sbbi.upnp.services;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * ISO8601 Date implementation taken from org.w3c package and modified to work with UPNP date types
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class ISO8601Date {
	private static boolean check(StringTokenizer st, String token) throws NumberFormatException {
		try {
			if (st.nextToken().equals(token)) {
				return true;
			} else {
				throw new NumberFormatException("Missing [" + token + "]");
			}
		} catch (NoSuchElementException ex) {
			return false;
		}
	}

	private static Calendar getCalendar(String isodate) throws NumberFormatException {
		// YYYY-MM-DDThh:mm:ss.sTZD or hh:mm:ss.sTZD
		// does it contains a date ?
		boolean isATime = isodate.indexOf(':') != -1;
		boolean isADate = isodate.indexOf('-') != -1 || (isodate.length() == 4 && !isATime);
		if (isATime && !isADate) {
			if (!isodate.toUpperCase().startsWith("T")) {
				isodate = "T" + isodate;
			}
		}
		StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.clear();
		if (isADate) {
			// Year
			if (st.hasMoreTokens()) {
				int year = Integer.parseInt(st.nextToken());
				calendar.set(Calendar.YEAR, year);
			} else {
				return calendar;
			}
			// Month
			if (check(st, "-") && (st.hasMoreTokens())) {
				int month = Integer.parseInt(st.nextToken()) - 1;
				calendar.set(Calendar.MONTH, month);
			} else {
				return calendar;
			}
			// Day
			if (check(st, "-") && (st.hasMoreTokens())) {
				int day = Integer.parseInt(st.nextToken());
				calendar.set(Calendar.DAY_OF_MONTH, day);
			} else {
				return calendar;
			}
		}
		// Hour    
		if ((check(st, "T")) && (st.hasMoreTokens())) {
			int hour = Integer.parseInt(st.nextToken());
			calendar.set(Calendar.HOUR_OF_DAY, hour);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar;
		}
		// Minutes
		if (check(st, ":") && (st.hasMoreTokens())) {
			int minutes = Integer.parseInt(st.nextToken());
			calendar.set(Calendar.MINUTE, minutes);
		} else {
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar;
		}

		//
		// Not mandatory now
		//

		// Secondes
		if (!st.hasMoreTokens()) {
			return calendar;
		}
		String tok = st.nextToken();
		if (tok.equals(":")) { // secondes
			if (st.hasMoreTokens()) {
				int secondes = Integer.parseInt(st.nextToken());
				calendar.set(Calendar.SECOND, secondes);
				if (!st.hasMoreTokens()) {
					return calendar;
				}
				// frac sec
				tok = st.nextToken();
				if (tok.equals(".")) {
					// bug fixed, thx to Martin Bottcher
					String nt = st.nextToken();
					while (nt.length() < 3) {
						nt += "0";
					}
					nt = nt.substring(0, 3); // Cut trailing chars..
					int millisec = Integer.parseInt(nt);
					// int millisec = Integer.parseInt(st.nextToken()) * 10;
					calendar.set(Calendar.MILLISECOND, millisec);
					if (!st.hasMoreTokens()) {
						return calendar;
					}
					tok = st.nextToken();
				} else {
					calendar.set(Calendar.MILLISECOND, 0);
				}
			} else {
				throw new NumberFormatException("No secondes specified");
			}
		} else {
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}
		// Timezone
		if (!tok.equals("Z")) { // UTC
			if (!(tok.equals("+") || tok.equals("-"))) {
				throw new NumberFormatException("only Z, + or - allowed");
			}
			boolean plus = tok.equals("+");
			if (!st.hasMoreTokens()) {
				throw new NumberFormatException("Missing hour field");
			}
			int tzhour = Integer.parseInt(st.nextToken());
			int tzmin = 0;
			if (check(st, ":") && (st.hasMoreTokens())) {
				tzmin = Integer.parseInt(st.nextToken());
			} else {
				throw new NumberFormatException("Missing minute field");
			}
			if (plus) {
				calendar.add(Calendar.HOUR, -tzhour);
				calendar.add(Calendar.MINUTE, -tzmin);
			} else {
				calendar.add(Calendar.HOUR, tzhour);
				calendar.add(Calendar.MINUTE, tzmin);
			}
		}
		return calendar;
	}

	/**
	 * Parse the given string in ISO 8601 format and build a Date object.
	 * 
	 * @param isodate
	 *            the date in ISO 8601 format
	 * @return a Date instance
	 * @exception InvalidDateException
	 *                if the date is not valid
	 */
	public static Date parse(String isodate) throws NumberFormatException {
		Calendar calendar = getCalendar(isodate);
		return calendar.getTime();
	}

	private static String twoDigit(int i) {
		if (i >= 0 && i < 10) {
			return "0" + String.valueOf(i);
		}
		return String.valueOf(i);
	}

	/**
	 * Generate a ISO 8601 date
	 * 
	 * @param date
	 *            a Date instance
	 * @return a string representing the date in the ISO 8601 format
	 */
	public static String getIsoDate(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		StringBuffer buffer = new StringBuffer();
		buffer.append(calendar.get(Calendar.YEAR));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
		return buffer.toString();
	}

	/**
	 * Generate a ISO 8601 date time without timezone
	 * 
	 * @param date
	 *            a Date instance
	 * @return a string representing the date in the ISO 8601 format
	 */
	public static String getIsoDateTime(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		StringBuffer buffer = new StringBuffer();
		buffer.append(calendar.get(Calendar.YEAR));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
		buffer.append("T");
		buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
		buffer.append(".");
		buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
		return buffer.toString();
	}

	/**
	 * Generate a ISO 8601 date time with timezone
	 * 
	 * @param date
	 *            a Date instance
	 * @return a string representing the date in the ISO 8601 format
	 */
	public static String getIsoDateTimeZone(Date date) {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.setTime(date);
		StringBuffer buffer = new StringBuffer();
		buffer.append(calendar.get(Calendar.YEAR));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
		buffer.append("T");
		buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
		buffer.append(".");
		buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
		buffer.append("Z");
		return buffer.toString();
	}

	/**
	 * Generate a ISO 8601 time
	 * 
	 * @param date
	 *            a Date instance
	 * @return a string representing the date in the ISO 8601 format
	 */
	public static String getIsoTime(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		StringBuffer buffer = new StringBuffer();
		buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
		buffer.append(".");
		buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
		return buffer.toString();
	}

	/**
	 * Generate a ISO 8601 time
	 * 
	 * @param date
	 *            a Date instance
	 * @return a string representing the date in the ISO 8601 format
	 */
	public static String getIsoTimeZone(Date date) {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.setTime(date);
		StringBuffer buffer = new StringBuffer();
		buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
		buffer.append(".");
		buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
		buffer.append("Z");
		return buffer.toString();
	}
}
