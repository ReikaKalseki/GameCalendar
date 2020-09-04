package Reika.GameCalendar.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import Reika.GameCalendar.Util.DateStamp;

public enum ImportantDates {

	EASTER(Month.APRIL, 4),
	THANKSGIVING(Month.OCTOBER, 12), //"second monday of october"
	HALLOWEEN(Month.OCTOBER, 31),
	CHRISTMAS(Month.DECEMBER, 25);

	private final Month month;
	private final int day;

	public static final ImportantDates[] dates = values();

	private ImportantDates(Month m, int d) {
		month = m;
		day = d;
	}

	public DateStamp getDate(int year) {
		switch(this) {
			case EASTER:
				return this.getEasterFor(year);
			case THANKSGIVING:
				return this.getThanksgivingFor(year);
			default:
				return new DateStamp(year, month, day);
		}
	}

	public String display() {
		char c = this.name().charAt(0);
		String s = this.name().substring(1).toLowerCase(Locale.ENGLISH);
		return c+s;
	}

	/**
	 * Easter - compute the day on which Easter falls.
	 *
	 * In the Christian religion, Easter is possibly the most important holiday of
	 * the year, so getting its date <I>just so </I> is worthwhile.
	 *
	 * @author: Ian F. Darwin, http://www.darwinsys.com/, based on a detailed
	 *          algorithm in Knuth, vol 1, pg 155.
	 *
	 * @Version: $Id: Easter.java,v 1.5 2004/02/09 03:33:46 ian Exp $ Written in C,
	 *           Toronto, 1988. Java version 1996.
	 *
	 * @Note: It's not proven correct, although it gets the right answer for years
	 *        around the present.
	 */
	private DateStamp getEasterFor(int year) {
		int golden, century, x, z, d, epact, n;

		golden = (year % 19) + 1; /* E1: metonic cycle */
		century = (year / 100) + 1; /* E2: e.g. 1984 was in 20th C */
		x = (3 * century / 4) - 12; /* E3: leap year correction */
		z = ((8 * century + 5) / 25) - 5; /* E3: sync with moon's orbit */
		d = (5 * year / 4) - x - 10;
		epact = (11 * golden + 20 + z - x) % 30; /* E5: epact */
		if ((epact == 25 && golden > 11) || epact == 24)
			epact++;
		n = 44 - epact;
		n += 30 * (n < 21 ? 1 : 0); /* E6: */
		n += 7 - ((d + n) % 7);
		if (n > 31) /* E7: */
			return new DateStamp(year, Month.APRIL, n - 31);
		else
			return new DateStamp(year, Month.MARCH, n);
	}

	private DateStamp getThanksgivingFor(int year) {
		LocalDate now = LocalDate.of(year, month.getValue(), day);
		LocalDate secondMonday = now.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)).plusDays(7);
		return new DateStamp(year, month, secondMonday.getDayOfMonth());
	}

}
