/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.integration.xml.transformer;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.integration.message.MessagingException;
import org.springframework.integration.transformer.PayloadTransformer;
import org.springframework.integration.xml.source.DomSourceFactory;
import org.springframework.integration.xml.source.SourceFactory;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

/**
 * An implementation of {@link PayloadTransformer} that delegates to an OXM
 * {@link Unmarshaller}. Expects the payload to be of type {@link Document},
 * {@link String}, {@link Source} or to have an instance of
 * {@link SourceFactory} that can convert to a {@link Source}. If
 * alwaysUseSourceFactory is set to true then the {@link SourceFactory} will
 * be used to create the {@link Source} regardless of payload type
 * 
 * 
 * @author Jonas Partner
 */
public class XmlPayloadUnmarshallingTransformer implements
		PayloadTransformer<Object, Object> {

	private volatile boolean alwaysUseSourceFactory = false;

	private final Unmarshaller unmarshaller;
	
	private volatile SourceFactory sourceFactory = new DomSourceFactory();

	public XmlPayloadUnmarshallingTransformer(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	public Object transform(Object payload) {
		Source source = null;
		if (alwaysUseSourceFactory) {
			source = sourceFactory.createSource(payload);
		} else if (payload instanceof String) {
			source = new StringSource((String) payload);
		} else if (payload instanceof Document) {
			source = new DOMSource((Document) payload);
		} else if (payload instanceof Source) {
			source = (Source) payload;
		} else {
			source = this.sourceFactory.createSource(payload);
		}

		if (source == null) {
			throw new MessagingException(
					"Failed to transform message, payload not assignable from javax.xml.transform.Source and no conversion possible");
		}

		try {
			return this.unmarshaller.unmarshal(source);
		} catch (IOException e) {
			throw new MessagingException("Failed to unamrshal payload", e);
		}
	}

	/**
	 * If true always delegate to the {@link SourceFactory}
	 * @param alwaysUseSourceFactory
	 */
	public void setAlwaysUseSourceFactory(boolean alwaysUseSourceFactory) {
		this.alwaysUseSourceFactory = alwaysUseSourceFactory;
	}

	/**
	 * 
	 * @param sourceFactory
	 */
	public void setSourceFactory(SourceFactory sourceFactory) {
		Assert.notNull(sourceFactory, "SourceFactory can not be null");
		this.sourceFactory = sourceFactory;
	}

}
