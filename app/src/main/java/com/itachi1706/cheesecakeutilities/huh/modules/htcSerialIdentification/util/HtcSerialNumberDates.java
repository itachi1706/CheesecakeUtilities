package com.itachi1706.cheesecakeutilities.huh.modules.htcSerialIdentification.util;

/**
 * Created by Kenneth on 3/20/2016.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
public enum HtcSerialNumberDates {
    ONE("1", 2011, "January", 1),
    TWO("2", 2012, "Feburary", 2),
    THREE("3", 2013, "March", 3),
    FOUR("4", 2014, "April", 4),
    FIVE("5", 2015, "May", 5),
    SIX("6", 2016, "June", 6),
    SEVEN("7", 2017, "July", 7),
    EIGHT("8", 2018, "August", 8),
    NINE("9", 2019, "September", 9),
    ZERO("0", 2010, "-", 0),
    A("A", 2020, "October", 10),
    B("B", 2021, "November", 11),
    C("C", 2022, "December", 12),
    D("D", 2023, "-", 13),
    E("E", 2024, "-", 14),
    F("F", 2025, "-", 15),
    G("G", 2026, "-", 16),
    H("H", 2027, "-", 17),
    I("I", 2028, "-", 18),
    J("J", 2029, "-", 19),
    K("K", 2030, "-", 20),
    L("L", 2031, "-", 21),
    M("M", 2032, "-", 22),
    N("N", 2033, "-", 23),
    O("O", 2034, "-", 24),
    P("P", 2035, "-", 25),
    Q("Q", 2036, "-", 26),
    R("R", 2037, "-", 27),
    S("S", 2038, "-", 28),
    T("T", 2039, "-", 29),
    U("U", 2040, "-", 30),
    V("V", 2041, "-", 31),
    W("W", 2042, "-", 0),
    X("X", 2043, "-", 0),
    Y("Y", 2044, "-", 0),
    Z("Z", 2045, "-", 0),
    UNKNOWN("-", 0, "-", 0);

    private String code, month;
    private int year, date;

    HtcSerialNumberDates(String code, int year, String month, int date){
        this.code = code;
        this.year = year;
        this.month = month;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public String getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getDate() {
        return date;
    }

    public static HtcSerialNumberDates getData(String code){
        for (HtcSerialNumberDates d : HtcSerialNumberDates.values()){
            if (d.getCode().equals(code))
                return d;
        }
        return UNKNOWN;
    }
}
