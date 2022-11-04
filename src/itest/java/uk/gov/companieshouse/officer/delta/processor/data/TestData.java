package uk.gov.companieshouse.officer.delta.processor.data;

import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestData {

    public static String getInputData(String officerType) {
        String path = "src/itest/resources/data/" + officerType + "_officer_delta_in.json";
        return readFile(path);
    }

    public static String getOutputData(String officerType) {
        String path = "src/itest/resources/data/" + officerType + "_officer_delta_out.json";
        return readFile(path).replaceAll("\n", "");
    }

    private static String readFile(String path) {
        String data;
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(new File(path))));
        } catch (IOException e) {
            data = null;
        }
        return data;
    }
}
