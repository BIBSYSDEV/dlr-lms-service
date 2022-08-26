package sikt.lti.tp.aws;

import java.util.HashMap;
import java.util.Map;

public class QueryStringParametersBuilder {
    private final Map<String, String> queryStringParameters = new HashMap<>();

    public QueryStringParametersBuilder() {
    }

    public QueryStringParametersBuilder withParameter(String name, String value) {
        queryStringParameters.put(name, value);
        return this;
    }

    public Map<String, String> build() {
        return this.queryStringParameters;
    }
}
