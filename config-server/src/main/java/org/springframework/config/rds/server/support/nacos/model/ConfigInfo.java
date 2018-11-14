/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.config.rds.server.support.nacos.model;

/**
 * 配置信息类
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class ConfigInfo extends ConfigInfoBase {
	static final long serialVersionUID = -1L;

	private String tenant;

	private String appName;

	public ConfigInfo() {

	}

	public ConfigInfo(String dataId, String group, String content) {
		super(dataId, group, content);
	}

	public ConfigInfo(String dataId, String group, String appName, String content) {
		super(dataId, group, content);
		this.appName = appName;
	}
	
	public ConfigInfo(String dataId, String group, String tenant, String appName, String content) {
		super(dataId, group, content);
		this.tenant = tenant;
		this.appName = appName;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return "ConfigInfo{" + "id=" + getId() + ", dataId='" + getDataId() + '\'' + ", group='" + getGroup() + '\''
				+ ", tenant='" + tenant + '\'' + ", appName='" + appName + '\'' + ", content='" + getContent() + '\''
				+ ", md5='" + getMd5() + '\'' + '}';
	}

}