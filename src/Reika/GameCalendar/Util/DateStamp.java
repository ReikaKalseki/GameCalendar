package Reika.GameCalendar.Util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.lwjglx.debug.joptsimple.internal.Strings;

import Reika.GameCalendar.Main;

public class DateStamp implements Comparable<DateStamp> {

	public final int year;
	public final Month month;
	public final int day;

	private static final Calendar calendar = Main.getCalendar();

	public static final DateStamp launch = DateStamp.now();

	public static final DateStamp xmasStart = new DateStamp(0, Month.DECEMBER, 7);
	public static final DateStamp xmasFull = new DateStamp(0, Month.DECEMBER, 19);
	public static final DateStamp xmasEnd = new DateStamp(0, Month.JANUARY, 11);
	public static final DateStamp readingStart = new DateStamp(0, Month.FEBRUARY, 13);
	public static final DateStamp readingEnd = new DateStamp(0, Month.FEBRUARY, 19);
	public static final DateStamp summerStart = new DateStamp(0, Month.APRIL, 16);
	public static final DateStamp summerFull = new DateStamp(0, Month.MAY, 1);
	public static final DateStamp summerEnd = new DateStamp(0, Month.SEPTEMBER, 8);

	public DateStamp(int y, Month m, int d) {
		year = y;
		month = m;
		day = d;
	}

	private static DateStamp now() {
		calendar.setTime(new Date(System.currentTimeMillis()));
		int year = calendar.get(Calendar.YEAR);
		Month month = Month.of(calendar.get(Calendar.MONTH)+1);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		return new DateStamp(year, month, day);
	}

	public Date getDate() {
		calendar.set(year, month.ordinal(), day);
		return calendar.getTime();
	}

	/** This assumes all months are given equal 1/12th fractions of the circle, not split proportionally. */
	public double getAngle() {
		/*
		calendar.set(year, month.ordinal(), day);
		int dy = calendar.get(Calendar.DAY_OF_YEAR);
		return dy/(isLeapYear() ? 366 : 365);
		 */
		double mf = day/(double)month.length(this.isLeapYear(year));
		return (month.ordinal()+mf)/12D*360D;
	}

	public static boolean isLeapYear(int year) {
		return year%4 == 0 && (year%100 != 0 || year%400 == 0);
	}

	@Override
	public int compareTo(DateStamp o) {
		return 50*(500*Integer.compare(year, o.year)+month.compareTo(o.month))+Integer.compare(day, o.day);
	}

	@Override
	public String toString() {
		return day+"/"+month.getValue()+"/"+year;
	}

	@Override
	public int hashCode() {
		return (month.hashCode() ^ year) * 247 - ~day;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DateStamp) {
			DateStamp d = (DateStamp)o;
			return d.day == day && d.month == month && d.year == year;
		}
		return false;
	}

	public static DateStamp parse(String s) {
		if (Strings.isNullOrEmpty(s))
			return null;
		if (s.equalsIgnoreCase("<ongoing>")) {
			return launch;
		}
		if (s.equalsIgnoreCase("<today>")) {
			return launch;
		}
		String[] parts = null;
		if (s.indexOf(' ') >= 0) {
			parts = s.split(" ");
		}
		else {
			parts = s.split("/");
		}
		int day = parts.length == 3 ? Integer.parseInt(parts[0]) : 1;
		Month month = parseMonth(parts.length == 3 ? parts[1] : parts[0]);
		int year = Integer.parseInt(parts.length == 3 ? parts[2] : parts[1]);
		return new DateStamp(year, month, day);
	}

	private static Month parseMonth(String val) {
		try {
			return Month.of(Integer.parseInt(val));
		}
		catch (NumberFormatException e) {
			switch(val.toLowerCase(Locale.ENGLISH)) {
				case "jan":
					return Month.JANUARY;
				case "feb":
					return Month.FEBRUARY;
				case "mar":
					return Month.MARCH;
				case "apr":
					return Month.APRIL;
				case "may":
					return Month.MAY;
				case "jun":
				case "june":
					return Month.JUNE;
				case "jul":
				case "july":
					return Month.JULY;
				case "aug":
					return Month.AUGUST;
				case "sep":
				case "sept":
					return Month.SEPTEMBER;
				case "oct":
					return Month.OCTOBER;
				case "nov":
					return Month.NOVEMBER;
				case "dec":
					return Month.DECEMBER;
				default:
					return null;
			}
		}
	}

	/** INCLUSIVE */
	public boolean isBetween(DateStamp start, DateStamp end) {
		return this.compareTo(start) >= 0 && this.compareTo(end) <= 0;
	}

	public static DateStamp fromDayOfYear(int year, int day) {
		calendar.set(Calendar.DAY_OF_YEAR, day);
		calendar.set(Calendar.YEAR, year);
		int month = calendar.get(Calendar.MONTH);
		int daym = calendar.get(Calendar.DAY_OF_MONTH);
		return new DateStamp(year, Month.of(month+1), daym);
	}

	public DateStamp getOffset(int years, int days) {
		LocalDate now = LocalDate.of(year, month.getValue(), day);
		LocalDate offset = now.plusDays(days).plusYears(years);
		return new DateStamp(offset.getYear(), offset.getMonth(), offset.getDayOfMonth());
	}

	public DateStamp previousDay() {
		if (month == Month.JANUARY && day == 1)
			return new DateStamp(year-1, Month.DECEMBER, 31);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month.ordinal());
		int dayd = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.set(Calendar.DAY_OF_YEAR, dayd-1);
		return new DateStamp(year, Month.of(calendar.get(Calendar.MONTH)+1), calendar.get(Calendar.DAY_OF_MONTH));
	}

	public DateStamp nextDay() {
		if (month == Month.DECEMBER && day == 31)
			return new DateStamp(year+1, Month.JANUARY, 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month.ordinal());
		int dayd = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.set(Calendar.DAY_OF_YEAR, dayd+1);
		return new DateStamp(year, Month.of(calendar.get(Calendar.MONTH)+1), calendar.get(Calendar.DAY_OF_MONTH));
	}

	public int countDaysAfter(DateStamp date) {
		LocalDate d1 = LocalDate.of(year, month, day);
		LocalDate d2 = LocalDate.of(date.year, date.month, date.day);
		return (int)ChronoUnit.DAYS.between(d1, d2);
	}

	public String getFullName(boolean fullMonthName) {
		return month.getDisplayName(fullMonthName ? TextStyle.FULL : TextStyle.SHORT, Locale.getDefault())+" "+day+", "+year;
	}
}
