package com.mycompany.app;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.junit.Test;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class LhvTest
{
    public void openTestUrl() { // URL avamine ja küpsistega nõustumine
        SelenideElement cookie = $("#acceptPirukas");
        Configuration.browser = "chrome";
        open("https://www.lhv.ee/et/liising#kalkulaator");

        // Kuna küpsiste vaade segab testi kulgemist, siis on lisatud if else check
        if (cookie.isDisplayed()) {
            $("#acceptPirukas").click(); // Küpsistega nõustumine
        } else {
            // Tegevust pole vaja, jätkab nii kuidas on
        }
    }

    @Test
    public void userCanSeeAllFields()
    {
        openTestUrl();
        $("#kalkulaator").shouldHave(text("Arvuta kuumakse"));
        $("#monthly-payment").should(exist);
    }

    @Test
    public void userCanIncludeVATAsLegalPerson()
    {
        openTestUrl();
        SelenideElement kaibemaksCheckbox = $("#vat_included");
        SelenideElement kaibemaksTasumine = $("#vat_scheduling");

        $("#account_type-1").click(); // Valib juriidilise isiku
        kaibemaksCheckbox.should(exist); // Nähtavale tekib "Hind sisaldab käibemaksu" checkbox
        kaibemaksTasumine.should(be(visible)); // ja käibemaksu tasumise viis

        $("#account_type-0").click(); // Valib eraisiku
        kaibemaksCheckbox.should(not(be(visible))); // "Hind sisaldab käibemaksu" checkbox
        kaibemaksTasumine.should(not(be(visible))); // ja käibemaksu tasumise viis pole enam nähtavad
    }

    @Test
    public void userCanIncludeVATForKasutusrent()
    {
        openTestUrl();
        SelenideElement kaibemaksCheckbox = $("#vat_included");

        $("#kas_rent").click(); // Valib kasutusrendi
        kaibemaksCheckbox.should(exist); // Nähtavale tekib "Hind sisaldab käibemaksu" checkbox

        $("#kap_rent").click(); // Valib kapitalirendi
        kaibemaksCheckbox.should(not(be(visible))); // Checkbox pole enam nähtaval
    }

    @Test
    public void userCanEnterNumbersIntoAllFields()
    {
        openTestUrl();

        $("#price").val("20000"); // Täidame vajaliku väljad
        $("#initial_percentage").val("20");
        $("#interest_rate").val("8");
        $("#reminder_percentage").val("30");

        // Kinnitame, et protsendid ja kuumakse on õigesti arvutatud
        $("#initial").shouldHave(value("4000"));
        $("#reminder").shouldHave(value("6000"));
        $(byClassName("payment")).shouldHave(text("242.76")).should(exist);
    }

    @Test
    public void monthlyPaymentIsZeroIfPeriodIsZero()
    {
        openTestUrl();
        $(byName("years")).selectOption(0); // Nullime perioodi väljad
        $(byName("months")).selectOption(0);

        $(byClassName("payment")).shouldHave(text("0.00")); // Kuumakse on 0€
    }

    @Test
    public void monthlyPaymentIsZeroIfInterestIsZero()
    {
        openTestUrl();
        $("#interest_rate").val("0"); // Nullime intressi

        $(byClassName("payment")).shouldHave(text("0.00")); // Kuumakse on 0€
    }

    @Test
    public void userCantConfigureMaximumPaymentIfIncomeLessThan1000()
    {
        openTestUrl();
        $(byText("Maksimaalne kuumakse")).click();
        $("#max-payment").should(be(visible)); // Kinnitame, et maksimaalne kuumakse on valitud

        $("#dependent-persons").selectOption(4);
        $("#monthly-income").val("999"); // Täidame välja alla tingimuse ehk alla 1000€
        // Kinnitame, et kasutajale antakse teada vähesest neto sissetulekust
        $("#max-payment").shouldHave(text("Maksimaalse kuumakse arvutamiseks on netosissetulek liiga väike."));
        $(byText("Näidiskuumakse")).click();
    }
}
