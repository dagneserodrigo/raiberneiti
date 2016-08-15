package br.com.raiberneiti;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention( RetentionPolicy.RUNTIME )
public @interface AlternateKey {
	public String keyName();
}