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

package org.apache.rocketmq.spring.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.util.ClassUtils;


/**
 * @see MessageConverter
 * @see CompositeMessageConverter
 *
 * @author zkz
 */
public class RocketMQMessageConverter {


	private static final boolean jackson2Present;
	private static final boolean fastJsonPresent;

	static {
		ClassLoader classLoader = RocketMQMessageConverter.class.getClassLoader();
		jackson2Present =
				ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", classLoader) &&
						ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", classLoader);
		fastJsonPresent = ClassUtils.isPresent("com.alibaba.fastjson.JSON",classLoader)&&
				ClassUtils.isPresent("com.alibaba.fastjson.support.config.FastJsonConfig",classLoader);
	}



	private final CompositeMessageConverter messageConverter;

	public RocketMQMessageConverter() {
		List<MessageConverter> messageConverters = new ArrayList<>();
		ByteArrayMessageConverter byteArrayMessageConverter = new ByteArrayMessageConverter();
		byteArrayMessageConverter.setContentTypeResolver(null);
		messageConverters.add(byteArrayMessageConverter);
		messageConverters.add(new StringMessageConverter());
		if(jackson2Present) {
			messageConverters.add(new MappingJackson2MessageConverter());
		}
		if(fastJsonPresent){
			try {
				messageConverters.add((MessageConverter)
						ClassUtils.forName("com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter"
								,ClassUtils.getDefaultClassLoader()).newInstance());
			} catch (ClassNotFoundException |IllegalAccessException |InstantiationException e) {
			}
		}
		messageConverter = new CompositeMessageConverter(messageConverters);
	}

	public MessageConverter getMessageConverter() {
		return messageConverter;
	}

	public MessageConverter resetMessageConverter(
			Collection<MessageConverter> converters) {
		if (messageConverter.getConverters() != null) {
			messageConverter.getConverters().clear();
		}
		Objects.requireNonNull(messageConverter.getConverters()).addAll(converters);
		return messageConverter;
	}

	public MessageConverter addMessageConverter(MessageConverter converter) {
		if (messageConverter.getConverters() != null && converter != null) {
			messageConverter.getConverters().add(converter);
		}
		return messageConverter;
	}

}
