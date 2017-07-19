/*******************************************************************************
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package gov.redhawk.core.graphiti.ui.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;

import gov.redhawk.core.graphiti.ui.ext.RHContainerShape;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.sad.HostCollocation;
import mil.jpeojtrs.sca.util.ScaEcoreUtils;

public class LayoutUtil {

	// Spacing/distance constants
	private static final int DIAGRAM_SHAPE_HORIZONTAL_PADDING = 100;
	private static final int HOST_COLLOCATION_SHAPE_HORIZONTAL_PADDING = 150;
	private static final int DIAGRAM_SHAPE_SIBLING_VERTICAL_PADDING = 5;
	private static final int DIAGRAM_SHAPE_ROOT_VERTICAL_PADDING = 50;

	protected LayoutUtil() {

	}

	/**
	 * @see {@link #calculateContainerBounds(ContainerShape, HostCollocation)}
	 * @param containerShape
	 * @return
	 */
	public static Point calculateContainerBounds(ContainerShape containerShape) {
		return calculateContainerBounds(containerShape, null);
	}

	/**
	 * Calculate the width/height boundaries for the container shape.<br/>
	 * Used as part of the layout process to determine contained shape locations.
	 * 
	 * @param containerShape - Should be either the Diagram or a HostCollocation
	 * @return {@link Point} where x is the right-most bound and y is the bottom-most bound
	 */
	// TODO: Add comment
	public static Point calculateContainerBounds(ContainerShape containerShape, HostCollocation hostCollocation) {
		// get root all shapes in diagram, components, findby's etc
		List<ContainerShape> rootShapes = new ArrayList<ContainerShape>();
		for (Shape shape : containerShape.getChildren()) {
			if (shape instanceof RHContainerShape) {
				checkIfRoot((RHContainerShape) shape, rootShapes, containerShape);
			} else if (DUtil.getBusinessObject(shape) != null && DUtil.getBusinessObject(shape) instanceof HostCollocation) {
				checkIfHostCoRoot((ContainerShape) shape, rootShapes);
			}
		}

		// combine dimensions of each root tree to determine total dimension required to house all shapes in diagram
		int height = 0;
		int width = 0;
		for (ContainerShape shape : rootShapes) {
			Point childTreeDimension = calculateTreeDimensions(shape, hostCollocation);
			height += childTreeDimension.getY();
			// use largest width
			width = Math.max(childTreeDimension.getX(), width);
		}

		// add padding between roots
		height += DIAGRAM_SHAPE_ROOT_VERTICAL_PADDING * (rootShapes.size() - 1);

		Point point = StylesFactory.eINSTANCE.createPoint();
		point.setX(width);
		point.setY(height);
		return point;
	}

	/**
	 * If the provided {@link RHContainerShape} has no provides ports or it has ports with no connections than it is a
	 * root of a tree and is added to the list of rootShapes
	 * 
	 * @param rhContainerShape - Shape to be inspected
	 * @param rootShapes - List of rootShapes for the parent container
	 * @param containerShape
	 */
	private static void checkIfRoot(RHContainerShape rhContainerShape, List<ContainerShape> rootShapes, ContainerShape containerShape) {
		EObject bObj = DUtil.getBusinessObject(containerShape);
		boolean isHostCollocation = (bObj != null && bObj instanceof HostCollocation) ? true : false;

		if (!isHostCollocation) {
			EList<ProvidesPortStub> providesStubs = rhContainerShape.getProvidesPortStubs();
			List<Connection> incomingConnections = DUtil.getIncomingConnectionsContainedInContainerShape(rhContainerShape);
			if (providesStubs.size() < 1 || incomingConnections.size() < 1) {
				rootShapes.add(rhContainerShape);
			}
		} else {
			// HostCo children are root relative to the HostCollocation only, other components won't be considered
			EList<ProvidesPortStub> providesStubs = rhContainerShape.getProvidesPortStubs();
			List<Connection> incomingConnections = DUtil.getIncomingConnectionsContainedInContainerShape(rhContainerShape);

			// If there is no providesPort or incoming connections then this is automatically a root shape
			if (providesStubs.size() < 1 || incomingConnections.size() < 1) {
				rootShapes.add(rhContainerShape);
				return;
			}

			// Otherwise check connections. If all originating components are OUTSIDE of the HostCollocation, then this
			// is still a root shape, at least relative to the HostCollocation's internal layout
			EList<Shape> compShapes = containerShape.getChildren();
			for (Connection connection : incomingConnections) {
				RHContainerShape originatingComp = ScaEcoreUtils.getEContainerOfType(connection.getStart(), RHContainerShape.class);
				if (compShapes.contains(originatingComp)) {
					// Not a root shape
					return;
				}
			}

			// Only get here for root shapes. Add to list and return.
			rootShapes.add(rhContainerShape);
		}
	}

	/**
	 * Used to check if a HostCollocation itself should be treated as a root shape, or as a tree leaf.<br/>
	 * Determined by checking to see if any contained component has an incoming connection originating outside the
	 * HostCollocation.
	 * 
	 * @param hostCoShape - HostCollocation shape to be inspected
	 * @param rootShapes - List of rootShapes for the parent container
	 */
	private static void checkIfHostCoRoot(ContainerShape hostCoShape, List<ContainerShape> rootShapes) {
		boolean isHostCoRoot = true;

		EList<Shape> compShapes = hostCoShape.getChildren();
		out: for (Shape childShape : compShapes) {
			RHContainerShape compShape = (RHContainerShape) childShape;

			// Check all incoming connections for components inside the HostCollocation.
			// If there is at least one externally originating connection. This HostCollocation is not a root.
			List<Connection> connections = DUtil.getIncomingConnectionsContainedInContainerShape(compShape);
			for (Connection connection : connections) {
				RHContainerShape originatingComp = ScaEcoreUtils.getEContainerOfType(connection.getStart(), RHContainerShape.class);
				if (!compShapes.contains(originatingComp)) {
					isHostCoRoot = false;
					break out;
				}
			}
		}

		if (isHostCoRoot) {
			rootShapes.add(hostCoShape);
		}
	}

	/**
	 * Returns dimensions required to contain all shapes aligned in a horizontal tree diagram beginning with the
	 * provided root shape.
	 * @param rootShape
	 * @param hostCollocation - may be null
	 * @return
	 */
	private static Point calculateTreeDimensions(ContainerShape rootShape, HostCollocation hostCollocation) {
		if (hostCollocation != null) {
			return calculateTreeDimensions(rootShape, new HashSet<ContainerShape>(), hostCollocation);
		} else {
			return calculateTreeDimensions(rootShape, new HashSet<ContainerShape>());
		}
	}

	/**
	 * Internal method used by {@link #calculateTreeDimensions(RHContainerShape)}.
	 * @param rootShape
	 * @return
	 */
	private static Point calculateTreeDimensions(ContainerShape rootShape, Set<ContainerShape> visitedShapes, HostCollocation hostCollocation) {
		// Keep track of the shape we're visiting; if we've been here, we're in a circular recursion
		if (!visitedShapes.add(rootShape)) {
			return null;
		}

		int height = rootShape.getGraphicsAlgorithm().getHeight();
		int width = rootShape.getGraphicsAlgorithm().getWidth();
		int childWidth = 0;
		int childHeight = 0;

		ContainerShape hostCoShape = null;
		if (DUtil.getBusinessObject(rootShape.getContainer()) instanceof HostCollocation) {
			hostCoShape = rootShape.getContainer();
		}

		List<Connection> outs = DUtil.getOutgoingConnectionsContainedInContainerShape(rootShape);
		for (Connection conn : outs) {
			RHContainerShape targetRHContainerShape = ScaEcoreUtils.getEContainerOfType(conn.getEnd(), RHContainerShape.class);

			// If the source is in HC, and the target is NOT in THE SAME HC, than do not consider the relationship
			// This works, because of the other calculateTreeDimensions which builds a top-level tree between the entire
			// HC and the target component
			if (hostCoShape != null && hostCoShape != targetRHContainerShape.getContainer()) {
				continue;
			}

			Point childDimension = null;
			childDimension = calculateTreeDimensions(targetRHContainerShape, visitedShapes, hostCollocation);
			if (childDimension == null) {
				continue;
			}
			childHeight += childDimension.getY() + DIAGRAM_SHAPE_SIBLING_VERTICAL_PADDING;

			// use largest width but don't add
			childWidth = Math.max(childDimension.getX(), childWidth);
		}
		if (outs.size() > 0) {
			if (DUtil.getBusinessObject(rootShape.getContainer()) instanceof HostCollocation) {
				width += childWidth;
			} else {
				width += childWidth + DIAGRAM_SHAPE_HORIZONTAL_PADDING;
			}
		}
		// choose the largest of parent height or combined child height
		height = Math.max(childHeight, height);

		Point point = StylesFactory.eINSTANCE.createPoint();
		point.setX(width);
		point.setY(height);
		return point;
	}

	/**
	 * Internal method used by {@link #calculateTreeDimensions(RHContainerShape)}.
	 * @param rootShape
	 * @return
	 */
	private static Point calculateTreeDimensions(ContainerShape rootShape, Set<ContainerShape> visitedShapes) {
		// Keep track of the shape we're visiting; if we've been here, we're in a circular recursion
		if (!visitedShapes.add(rootShape)) {
			return null;
		}

		int height = rootShape.getGraphicsAlgorithm().getHeight();
		int width = rootShape.getGraphicsAlgorithm().getWidth();
		int childWidth = 0;
		int childHeight = 0;

		List<Connection> outs = DUtil.getOutgoingConnectionsContainedInContainerShape(rootShape);
		for (Connection conn : outs) {
			RHContainerShape targetRHContainerShape = ScaEcoreUtils.getEContainerOfType(conn.getEnd(), RHContainerShape.class);
			Point childDimension = null;

			// At this level, we care about the dimensions of the HostCollocation, not of any contained shapes
			if (DUtil.getBusinessObject(targetRHContainerShape.getContainer()) instanceof HostCollocation) {
				childDimension = calculateTreeDimensions(targetRHContainerShape.getContainer(), visitedShapes);
			} else {
				childDimension = calculateTreeDimensions(targetRHContainerShape, visitedShapes);
			}
			if (childDimension == null) {
				continue;
			}
			childHeight += childDimension.getY() + DIAGRAM_SHAPE_SIBLING_VERTICAL_PADDING;

			// use largest width but don't add
			childWidth = Math.max(childDimension.getX(), childWidth);
		}
		if (outs.size() > 0) {
			if (DUtil.getBusinessObject(rootShape) instanceof HostCollocation) {
				// static padding size for HC based on num of children
				width += childWidth + rootShape.getChildren().size() * HOST_COLLOCATION_SHAPE_HORIZONTAL_PADDING;
			} else {
				width += childWidth + DIAGRAM_SHAPE_HORIZONTAL_PADDING;
			}
		}
		// choose the largest of parent height or combined child height
		height = Math.max(childHeight, height);

		Point point = StylesFactory.eINSTANCE.createPoint();
		point.setX(width);
		point.setY(height);
		return point;
	}
}