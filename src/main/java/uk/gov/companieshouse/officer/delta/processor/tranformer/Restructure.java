package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentApi;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class Restructure implements Transform {
    @Override
    public void transform(OfficersItem inputOfficer, AppointmentAPI outputAppointment) {
        String inputAppointed = inputOfficer.getAppointmentDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.UK);
        LocalDateTime outputAppointedOn = LocalDate.parse(inputAppointed, formatter).atStartOfDay();

        OfficerAPI officer = new OfficerAPI();

        officer.setAppointedOn(outputAppointedOn);
        officer.setCompanyNumber(inputOfficer.getCompanyNumber());
        officer.setForename(inputOfficer.getForename());
        officer.setOtherForenames(inputOfficer.getMiddleName());
        officer.setSurname(inputOfficer.getSurname());
        officer.setNationality(inputOfficer.getNationality());
        officer.setOccupation(inputOfficer.getOccupation());

        officer.setServiceAddress(inputOfficer.getServiceAddress());
        officer.setServiceAddressSameAsRegisteredOfficeAddress(setValueOfServiceAddress(inputOfficer.getServiceAddressSameAsRegisteredAddress()));

        outputAppointment.setData(officer);
    }

    private boolean setValueOfServiceAddress(final String serviceAddressSameAsRegisteredAddress) {
        return serviceAddressSameAsRegisteredAddress.equalsIgnoreCase("Y");
    }
}
