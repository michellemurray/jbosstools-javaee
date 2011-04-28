/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.cdi.ui.search;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jdt.ui.search.IQueryParticipant;
import org.eclipse.jdt.ui.search.ISearchRequestor;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PartInitException;
import org.jboss.tools.cdi.core.CDICoreNature;
import org.jboss.tools.cdi.core.CDICorePlugin;
import org.jboss.tools.cdi.core.IBean;
import org.jboss.tools.cdi.core.ICDIElement;
import org.jboss.tools.cdi.core.ICDIProject;
import org.jboss.tools.cdi.core.IClassBean;
import org.jboss.tools.cdi.core.IInjectionPoint;
import org.jboss.tools.cdi.core.IInjectionPointField;
import org.jboss.tools.cdi.core.IInjectionPointMethod;
import org.jboss.tools.cdi.core.IInjectionPointParameter;
import org.jboss.tools.cdi.ui.CDIUIMessages;
import org.jboss.tools.cdi.ui.CDIUIPlugin;
import org.jboss.tools.cdi.ui.CDIUiImages;

public class CDIBeanQueryParticipant implements IQueryParticipant{
	static CDIBeanLabelProvider labelProvider = new CDIBeanLabelProvider();
	
	@Override
	public void search(ISearchRequestor requestor,
			QuerySpecification querySpecification, IProgressMonitor monitor)
			throws CoreException {
		if(querySpecification instanceof ElementQuerySpecification){
			if (!isSearchForReferences(querySpecification.getLimitTo()))
				return;
			
			ElementQuerySpecification qs = (ElementQuerySpecification)querySpecification;
			IJavaElement element = qs.getElement();
			if(element instanceof IType){
				IFile file = (IFile)element.getResource();
				if(file == null)
					return;
				
				CDICoreNature cdiNature = CDICorePlugin.getCDI(file.getProject(), true);
				
				if(cdiNature == null)
					return;
				
				ICDIProject cdiProject = cdiNature.getDelegate();
				
				if(cdiProject == null)
					return;
				
				IClassBean sourceBean = cdiProject.getBeanClass((IType)element);
				
				IBean[] beans = cdiProject.getBeans();
				
				monitor.beginTask(CDIUIMessages.CDI_BEAN_QUERY_PARTICIPANT_TASK, beans.length);
				
				for(IBean bean : beans){
					if(monitor.isCanceled())
						break;
					
					monitor.worked(1);
					
					Set<IInjectionPoint> injectionPoints = bean.getInjectionPoints();
					
					for(IInjectionPoint injectionPoint : injectionPoints){
						Set<IBean> resultBeans = cdiProject.getBeans(false, injectionPoint);
						
						for(IBean cBean : resultBeans){
							if(cBean == sourceBean){
								Match match = new CDIMatch(injectionPoint);
								requestor.reportMatch(match);
							}
						}
					}
				}
				monitor.done();
			}
		}
	}
	
	public boolean isSearchForReferences(int limitTo) {
    	int maskedLimitTo = limitTo & ~(IJavaSearchConstants.IGNORE_DECLARING_TYPE+IJavaSearchConstants.IGNORE_RETURN_TYPE);
    	if (maskedLimitTo == IJavaSearchConstants.REFERENCES || maskedLimitTo == IJavaSearchConstants.ALL_OCCURRENCES) {
    		return true;
    	}
    
    	return false;
    }

	@Override
	public int estimateTicks(QuerySpecification specification) {
		return 10;
	}

	@Override
	public IMatchPresentation getUIParticipant() {
		return new CDIBeanMatchPresentation();
	}
	
	class CDIBeanMatchPresentation implements IMatchPresentation{

		@Override
		public ILabelProvider createLabelProvider() {
			return labelProvider;
		}

		@Override
		public void showMatch(Match match, int currentOffset,
				int currentLength, boolean activate) throws PartInitException {
			if(match instanceof CDIMatch){
				IJavaElement element = ((CDIMatch)match).getJavaElement();
				if(element != null){
					try{
						JavaUI.openInEditor(element);
					}catch(JavaModelException ex){
						CDIUIPlugin.getDefault().logError(ex);
					}catch(PartInitException ex){
						CDIUIPlugin.getDefault().logError(ex);
					}
				}
			}

		}
		
	}
	
	static class CDIBeanLabelProvider implements ILabelProvider{

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getImage(Object element) {
			return CDIUiImages.WELD_IMAGE;
		}

		@Override
		public String getText(Object element) {
			if(element instanceof CDIElementWrapper){
				ICDIElement cdiElement = ((CDIElementWrapper)element).getCDIElement();
				String label = ((CDIElementWrapper)element).getLabel();
				
				if(cdiElement instanceof IInjectionPointField){
					return NLS.bind(CDIUIMessages.CDI_BEAN_QUERY_PARTICIPANT_INJECT_FIELD, label);
				}else if(cdiElement instanceof IInjectionPointMethod){
					return NLS.bind(CDIUIMessages.CDI_BEAN_QUERY_PARTICIPANT_INJECT_METHOD, label);
				}else if(cdiElement instanceof IInjectionPointParameter){
					return NLS.bind(CDIUIMessages.CDI_BEAN_QUERY_PARTICIPANT_INJECT_PARAMETER, label);
				}
			}
			return ""; //$NON-NLS-1$

		}
		
	}

}
