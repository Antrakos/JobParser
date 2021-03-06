package com.olegshan.parser.job;

import com.olegshan.sites.JobSite;
import com.olegshan.tools.MonthsTools;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Taras Zubrei
 */
public class RabotaUaJobParser extends JobParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabotaUaJobParser.class);

    public RabotaUaJobParser(JobSite jobSite) {
        super(jobSite);
    }

    @Override
    public Elements getJobBlocks(Document doc) {
        Elements jobBlocks = new Elements();
        for (int i = 1; i < jobSite.getJobBox().length; i++) {
            Elements jobElements = doc.getElementsByAttributeValue(jobSite.getJobBox()[0], jobSite.getJobBox()[i]);
            if (jobElements != null && !jobElements.isEmpty()) {
                jobBlocks.addAll(jobElements);
            }
        }
        return jobBlocks;
    }

    @Override
    public LocalDateTime getDate(Element job, Document doc, Elements titleBlock) {
        /*
        * There are several problems here.
        * First: there are two types of date tags, used on rabota.ua on different pages: "d-date" and "datePosted".
        * Second: sometimes date format is dd.mm.yyyy and sometimes — yyyy-mm-dd.
        * Third: sometimes there is no date at all.
        */
        String dateLine;
        String[] dateParts;
        int year;
        int month;
        int day;

        Elements dateElements = doc.getElementsByAttributeValue("id", "d-date");
        if (!dateElements.isEmpty()) {
            dateLine = dateElements.get(0).getElementsByAttributeValue("class", "d-ph-value").text();
        } else {
            dateLine = doc.getElementsByAttributeValue("itemprop", "datePosted").text();
            if (dateLine.length() == 0) {
                //no date at all, sometimes it happens
                LocalDateTime ldt = LocalDateTime.now(ZoneId.of("Europe/Athens"));
                LOGGER.debug("There was no date on Rabota.ua, return {}", ldt);
                return ldt;
            }
        }
        try {
            dateParts = dateLine.split("\\.");
            MonthsTools.removeZero(dateParts);
            year = Integer.parseInt(dateParts[2]);
            month = Integer.parseInt(dateParts[1]);
            day = Integer.parseInt(dateParts[0]);

        } catch (ArrayIndexOutOfBoundsException e) {

            dateParts = dateLine.split("-");
            MonthsTools.removeZero(dateParts);
            year = Integer.parseInt(dateParts[0]);
            month = Integer.parseInt(dateParts[1]);
            day = Integer.parseInt(dateParts[2]);
        }
        return LocalDate.of(year, month, day).atTime(getTime());
    }

}
