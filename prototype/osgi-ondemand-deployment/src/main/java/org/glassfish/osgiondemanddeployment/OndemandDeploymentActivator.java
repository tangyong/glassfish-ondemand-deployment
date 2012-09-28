/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.osgiondemanddeployment;

import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OndemandDeploymentActivator, in order to intercepts methods calls to the bundle context to install bundles when services are required.
 *
 * @author Tang Yong(tangyong@cn.fujitsu.com)
 */
public class OndemandDeploymentActivator implements BundleActivator {

	protected BundleContext context = null;
	protected BundleContext interceptedContext = null;
	protected BundleActivator proxiedActivator = null;
	private static final String PROXIED_BUNDLE_ACTIVATOR_KEY = "Proxied-Bundle-Activator";	
	
	/**
	 * create then start an intercepted activator instance.
	 *
	 * @param   context  the bundle context passed by the framework
	 * @exception   Exception
	 */
	public void start(BundleContext context) throws Exception {
    	this.context = context;
							
		// Get the Proxied-Bundle-Activator value from the manifest of current bundle
		String proxiedActivatorStr = null;
		Dictionary<?,?> dict = context.getBundle().getHeaders();
		Enumeration<?> bundleMetadata = dict.keys();
		while (bundleMetadata.hasMoreElements()) {
			Object key = bundleMetadata.nextElement();
			if ( key.equals(PROXIED_BUNDLE_ACTIVATOR_KEY)) {
				proxiedActivatorStr = dict.get(key).toString();
				break;
			} 
		}
		
		if (proxiedActivatorStr != null){
			this.interceptedContext = new OndemandDeploymentBundleContextImpl(context);
			Class proxiedActivatorClass = this.getClass().getClassLoader().loadClass(proxiedActivatorStr);
			proxiedActivator = (BundleActivator) proxiedActivatorClass.newInstance();
			proxiedActivator.start(interceptedContext);
		} else {
			//ToDo:(TangYong) need to handle the case 
		}
	}
	
	public void stop(BundleContext context) throws Exception {
		if (proxiedActivator != null)
			proxiedActivator.stop(interceptedContext);
	}
}
