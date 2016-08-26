/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package gov.redhawk.core.graphiti.dcd.ui.diagram.feature;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

import gov.redhawk.core.graphiti.dcd.ui.diagram.providers.DeviceManagerImageProvider;
import gov.redhawk.core.graphiti.ui.util.DUtil;
import gov.redhawk.sca.util.PluginUtil;
import mil.jpeojtrs.sca.dcd.DcdComponentInstantiation;
import mil.jpeojtrs.sca.dcd.DcdComponentPlacement;
import mil.jpeojtrs.sca.dcd.DcdFactory;
import mil.jpeojtrs.sca.dcd.DeviceConfiguration;
import mil.jpeojtrs.sca.partitioning.ComponentFile;
import mil.jpeojtrs.sca.partitioning.ComponentFileRef;
import mil.jpeojtrs.sca.partitioning.ComponentFiles;
import mil.jpeojtrs.sca.partitioning.PartitioningFactory;
import mil.jpeojtrs.sca.sad.SadFactory;
import mil.jpeojtrs.sca.spd.SoftPkg;

public class ServiceCreateFeature extends AbstractCreateFeature {

	private SoftPkg spd = null;
	private String implId = null;
	public static final String SHAPE_TYPE = "serviceShape";
	public static final String OVERRIDE_USAGE_NAME = "OverrideUsageName";
	public static final String OVERRIDE_INSTANTIATION_ID = "OverrideInstantiationId";
	public static final String OVERRIDE_IMPLEMENTATION_ID = "OverrideImplementationId";

	@Override
	public String getDescription() {
		// Provides the context menu Undo/Redo description
		return "Add Service to Diagram";
	}

	public ServiceCreateFeature(IFeatureProvider fp, String name, String description) {
		super(fp, name, description);
	}

	public ServiceCreateFeature(IFeatureProvider fp, SoftPkg spd, String implId) {
		super(fp, spd.getName(), spd.getDescription());
		this.spd = spd;
		this.implId = implId;
	}

	@Override
	public boolean canCreate(ICreateContext context) {
		if (context.getTargetContainer() instanceof Diagram) {
			return true;
		}
		return false;
	}

	@Override
	public Object[] create(ICreateContext context) {

		if (spd == null) {
			// TODO: return some kind of error
			return null;
		}

		// collect overrides (currently used by GraphitiDcdModelMap)
		final String usageName = (String) context.getProperty(OVERRIDE_USAGE_NAME);
		final String instantiationId = (String) context.getProperty(OVERRIDE_INSTANTIATION_ID);
		final String implementationId = (String) context.getProperty(OVERRIDE_IMPLEMENTATION_ID);

		// editing domain for our transaction
		TransactionalEditingDomain editingDomain = getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();

		// Get dcd from diagram
		final DeviceConfiguration dcd = DUtil.getDiagramDCD(getDiagram());

		// Add to dcd partitioning
		final EList<DcdComponentPlacement> componentPlacementList = dcd.getPartitioning().getComponentPlacement();

		// container for new component instantiation, necessary for reference after command execution
		final DcdComponentInstantiation[] componentInstantiations = new DcdComponentInstantiation[1];

		// Create Service related objects in DCD model
		TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
		stack.execute(new RecordingCommand(editingDomain) {
			@Override
			protected void doExecute() {
				// add component file
				ComponentFile componentFile = createComponentFile(dcd, spd);

				// create component placement and add to list
				final DcdComponentPlacement componentPlacement = DcdFactory.eINSTANCE.createDcdComponentPlacement();
				componentPlacementList.add(componentPlacement);

				// create component file ref
				final ComponentFileRef ref = PartitioningFactory.eINSTANCE.createComponentFileRef();
				ref.setFile(componentFile);
				componentPlacement.setComponentFileRef(ref);

				// component instantiation
				componentInstantiations[0] = createComponentInstantiation(dcd, componentPlacement, spd, usageName, instantiationId, implementationId);
			}
		});

		// Call add feature
		context.putProperty("shapeType", SHAPE_TYPE);
		addGraphicalRepresentation(context, componentInstantiations[0]);

		return new Object[] { componentInstantiations[0] };
	}

	// adds corresponding component file to sad if not already present
	private ComponentFile createComponentFile(final DeviceConfiguration dcd, final SoftPkg spd) {

		// See if we have to add a new component file
		ComponentFile file = null;
		// set component files is not already set
		ComponentFiles cFiles = dcd.getComponentFiles();
		if (cFiles == null) {
			cFiles = PartitioningFactory.eINSTANCE.createComponentFiles();
			dcd.setComponentFiles(cFiles);
		}
		// search for existing compatible component file for spd
		for (final ComponentFile f : cFiles.getComponentFile()) {
			if (f == null) {
				continue;
			}
			final SoftPkg fSpd = f.getSoftPkg();
			if (fSpd != null && PluginUtil.equals(spd.getId(), fSpd.getId())) {
				file = f;
				break;
			}
		}
		// add new component file if not found above
		if (file == null) {
			file = SadFactory.eINSTANCE.createComponentFile();
			cFiles.getComponentFile().add(file);
			file.setSoftPkg(spd);
		}

		return file;
	}

	// create ComponentInstantiation
	private DcdComponentInstantiation createComponentInstantiation(final DeviceConfiguration dcd, DcdComponentPlacement componentPlacement, SoftPkg spd,
		final String providedUsageName, final String providedInstantiationId, final String providedImplId) {
		DcdComponentInstantiation dcdComponentInstantiation = DcdFactory.eINSTANCE.createDcdComponentInstantiation();

		String serviceName = (providedUsageName != null) ? providedUsageName : DeviceConfiguration.Util.createDeviceUsageName(dcd, spd.getName());
		String id = (providedInstantiationId != null) ? providedInstantiationId : dcd.getName() + ":" + serviceName;
		String implementationId = (providedImplId != null) ? providedImplId : implId;

		dcdComponentInstantiation.setUsageName(serviceName);
		dcdComponentInstantiation.setId(id);
		dcdComponentInstantiation.setImplID(implementationId);

		// add to placement
		componentPlacement.getComponentInstantiation().add(dcdComponentInstantiation);

		return dcdComponentInstantiation;
	}

	@Override
	public String getCreateImageId() {
		return DeviceManagerImageProvider.IMG_SERVICE;
	}

}
