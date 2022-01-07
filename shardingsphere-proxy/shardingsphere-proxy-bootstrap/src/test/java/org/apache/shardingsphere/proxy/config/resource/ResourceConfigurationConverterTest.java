/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.config.resource;

import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlResourceConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ResourceConfigurationConverterTest {
    
    @Test
    public void assertGetDataSourceConfigurationMap() {
        Map<String, ResourceConfiguration> resourceConfigMap = new HashMap<>(2, 1);
        resourceConfigMap.put("ds_0", createResourceConfiguration());
        resourceConfigMap.put("ds_1", createResourceConfiguration());
        Map<String, DataSourceConfiguration> actual = ResourceConfigurationConverter.getDataSourceConfigurationMap(resourceConfigMap);
        assertThat(actual.size(), is(2));
        assertParameter(actual.get("ds_0"));
        assertParameter(actual.get("ds_1"));
    }
    
    private ResourceConfiguration createResourceConfiguration() {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration("jdbc:mysql://localhost:3306/demo_ds", "root", "root");
        PoolConfiguration poolConfig = new PoolConfiguration(null, null, null, null, null, null, null);
        return new ResourceConfiguration(connectionConfig, poolConfig);
    }
    
    private void assertParameter(final DataSourceConfiguration actual) {
        Map<String, Object> props = actual.getProps();
        assertThat(props.size(), is(9));
        assertThat(props.get("jdbcUrl"), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(props.get("username"), is("root"));
        assertThat(props.get("password"), is("root"));
        assertNull(props.get("maximumPoolSize"));
        assertNull(props.get("minimumIdle"));
        assertNull(props.get("connectionTimeout"));
        assertNull(props.get("idleTimeout"));
        assertNull(props.get("maxLifetime"));
    }
    
    @Test
    public void assertGetResourceConfigurationMap() {
        YamlResourceConfiguration yamlResourceConfig0 = new YamlResourceConfiguration();
        yamlResourceConfig0.setUrl("jdbc:mysql://localhost:3306/ds_0");
        yamlResourceConfig0.setCustomPoolProps(getCustomPoolProperties());
        setYamlResourceConfigurationPropertyWithoutUrl(yamlResourceConfig0);
        YamlResourceConfiguration yamlResourceConfig1 = new YamlResourceConfiguration();
        yamlResourceConfig1.setUrl("jdbc:mysql://localhost:3306/ds_1");
        yamlResourceConfig1.setCustomPoolProps(getCustomPoolProperties());
        setYamlResourceConfigurationPropertyWithoutUrl(yamlResourceConfig1);
        Map<String, YamlResourceConfiguration> yamlResourceConfigs = new HashMap<>(2, 1);
        yamlResourceConfigs.put("ds_0", yamlResourceConfig0);
        yamlResourceConfigs.put("ds_1", yamlResourceConfig1);
        Map<String, ResourceConfiguration> actualResourceConfig = ResourceConfigurationConverter.getResourceConfigurationMap(yamlResourceConfigs);
        assertThat(actualResourceConfig.size(), is(2));
        assertThat(actualResourceConfig.get("ds_0").getConnection().getUrl(), is("jdbc:mysql://localhost:3306/ds_0"));
        assertThat(actualResourceConfig.get("ds_1").getConnection().getUrl(), is("jdbc:mysql://localhost:3306/ds_1"));
        assertResourceConfiguration(actualResourceConfig.get("ds_0"));
        assertResourceConfiguration(actualResourceConfig.get("ds_1"));
    }
    
    private void setYamlResourceConfigurationPropertyWithoutUrl(final YamlResourceConfiguration yamlResourceConfig) {
        yamlResourceConfig.setUsername("root");
        yamlResourceConfig.setPassword("root");
        yamlResourceConfig.setConnectionTimeoutMilliseconds(30 * 1000L);
        yamlResourceConfig.setIdleTimeoutMilliseconds(60 * 1000L);
        yamlResourceConfig.setMaxLifetimeMilliseconds(0L);
        yamlResourceConfig.setMaxPoolSize(50);
        yamlResourceConfig.setMinPoolSize(1);
    }
    
    private void assertResourceConfiguration(final ResourceConfiguration resourceConfig) {
        assertThat(resourceConfig.getConnection().getUsername(), is("root"));
        assertThat(resourceConfig.getConnection().getPassword(), is("root"));
        assertThat(resourceConfig.getPool().getConnectionTimeoutMilliseconds(), is(30 * 1000L));
        assertThat(resourceConfig.getPool().getIdleTimeoutMilliseconds(), is(60 * 1000L));
        assertThat(resourceConfig.getPool().getMaxLifetimeMilliseconds(), is(0L));
        assertThat(resourceConfig.getPool().getMaxPoolSize(), is(50));
        assertThat(resourceConfig.getPool().getMinPoolSize(), is(1));
        assertThat(resourceConfig.getPool().getCustomProperties().size(), is(2));
        assertThat(resourceConfig.getPool().getCustomProperties().get("maxPoolSize"), is(30));
        assertThat(resourceConfig.getPool().getCustomProperties().get("idleTimeoutMilliseconds"), is("30000"));
    }
    
    private Properties getCustomPoolProperties() {
        Properties result = new Properties();
        result.put("maxPoolSize", 30);
        result.put("idleTimeoutMilliseconds", "30000");
        return result;
    }
}
