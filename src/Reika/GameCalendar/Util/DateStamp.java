package Reika.GameCalendar.Util;

import java.time.Month;
import java.util.Calendar;
import java.util.Date;

import Reika.GameCalendar.Main;

public class DateStamp implements Comparable<DateStamp> {

	public final int year;
	public final Month month;
	public final int day;

	private static final Calendar calendar = Main.getCalendar();

	public static final DateStamp launch = DateStamp.now();

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
		String[] parts = s.split("/");
		int day = parts.length == 3 ? Integer.parseInt(parts[0]) : 1;
		int month = Integer.parseInt(parts.length == 3 ? parts[1] : parts[0]);
		int year = Integer.parseInt(parts.length == 3 ? parts[2] : parts[1]);
		return new DateStamp(year, Month.of(month), day);
	}
}
