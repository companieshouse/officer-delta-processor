package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class Restructure implements Transform {
    @Override
    public void transform(OfficersItem inputOfficer, OfficerAPI outputOfficer) {
        String inputAppointed = inputOfficer.getAppointmentDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.UK);
        LocalDateTime outputAppointment = LocalDate.parse(inputAppointed, formatter).atStartOfDay();

        outputOfficer.setAppointedOn(outputAppointment);
        outputOfficer.setCompanyNumber(inputOfficer.getCompanyNumber());
        outputOfficer.setForename(inputOfficer.getForename());
        outputOfficer.setOtherForenames(inputOfficer.getMiddleName());
        outputOfficer.setSurname(inputOfficer.getSurname());
        outputOfficer.setNationality(inputOfficer.getNationality());
        outputOfficer.setOccupation(inputOfficer.getNationality());

        outputOfficer.setServiceAddress(inputOfficer.getServiceAddress());
        outputOfficer.setServiceAddressSameAsRegisteredOfficeAddress(setValueOfServiceAddress(inputOfficer.getServiceAddressSameAsRegisteredAddress()));
    }

    private boolean setValueOfServiceAddress(final String serviceAddressSameAsRegisteredAddress) {
        if (serviceAddressSameAsRegisteredAddress.equals("Y")) {
            return Boolean.valueOf("true");
        }
        else return Boolean.valueOf("false");
    }
}
