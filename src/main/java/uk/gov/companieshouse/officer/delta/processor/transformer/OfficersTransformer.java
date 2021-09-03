package uk.gov.companieshouse.officer.delta.processor.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OfficersTransformer implements Transformative<Officers, List<AppointmentAPI>> {
    private final OfficerTransformer officerTransformer;

    public OfficersTransformer(final OfficerTransformer officerTransformer) {
        this.officerTransformer = officerTransformer;
    }

    @Override
    public List<AppointmentAPI> transform(final Officers source) {
        return officerTransformer.transform(source.getOfficers())
                .stream()
                .map(app -> {
                    app.setDeltaAt(source.getDeltaAt());
                    return app;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<List<AppointmentAPI>> transform(final Collection<Officers> sources) {
        return sources.stream().map(this::transform).collect(Collectors.toList());
    }
}
