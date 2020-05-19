package com.exemple.service.customer.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.customer.subcription.SubscriptionService;
import com.exemple.service.customer.subcription.exception.SubscriptionServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class SubscriptionServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private SubscriptionService service;

    @Autowired
    private SchemaFilter schemaFilter;

    private static final String APP = "default";

    private static final String VERSION = "default";

    private static final String PROFILE = "default";

    @BeforeMethod
    private void before() {

        Mockito.reset(resource, schemaFilter);

    }

    @DataProvider(name = "save")
    private static Object[][] update() {

        return new Object[][] {
                // created
                { Optional.empty(), true },
                // updated
                { Optional.of(JsonNodeUtils.init()), false }
                //
        };
    }

    @Test(dataProvider = "save")
    public void save(Optional<JsonNode> subscription, boolean expectedCreated) {

        String email = "jean@gmail.com";

        Mockito.when(resource.get(email)).thenReturn(subscription);

        boolean created = service.save(email, JsonNodeUtils.init(), APP, VERSION, PROFILE);

        Mockito.verify(resource).save(Mockito.eq(email), Mockito.any(JsonNode.class));

        assertThat(created, is(expectedCreated));

    }

    @Test
    public void get() throws SubscriptionServiceNotFoundException {

        String email = "jean@gmail.com";

        Mockito.when(schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.init());
        Mockito.when(resource.get(Mockito.eq(email))).thenReturn(Optional.of(JsonNodeUtils.init()));

        JsonNode data = service.get(email, APP, VERSION, PROFILE);

        assertThat(data, is(JsonNodeUtils.init()));

    }

    @Test(expectedExceptions = SubscriptionServiceNotFoundException.class)
    public void getNotFound() throws SubscriptionServiceNotFoundException {

        String email = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(email))).thenReturn(Optional.empty());

        service.get(email, APP, VERSION, PROFILE);

    }
}
