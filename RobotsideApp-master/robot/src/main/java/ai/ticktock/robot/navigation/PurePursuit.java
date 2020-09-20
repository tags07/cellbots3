package ai.cellbots.robot.navigation;

import ai.cellbots.common.Geometry;
import ai.cellbots.common.Transform;

/**
 * The concept of the pure pursuit approach is to calculate the curvature that will take the
 * vehicle from its current position to a goal position. A circle is then defined in such a way
 * that it passes through both the goal point and the current vehicle position. Finally a control
 * algorithm chooses a steering angle in relation to this circle.
 */
class PurePursuit {
    // Tweak parameter of the algorithm to modify the curvature generated by the algorithm into
    // angular movements for the robot
    private static final double ANGULAR_MODIFICATION = 1.0;
    // The robot sees behind a lookahead distance
    private double mLookaheadDistance;
    // Tweak parameter for curvature output
    private double mAngularModification;

    /**
     * Constructor
     *
     * @param lookaheadDistance Distance to consider from the current pose
     */
    PurePursuit(double lookaheadDistance) {
        this(lookaheadDistance, ANGULAR_MODIFICATION);
    }

    PurePursuit(double lookaheadDistance, double angularModification) {
        // Initialization
        this.mLookaheadDistance = lookaheadDistance;
        this.mAngularModification = angularModification;
    }

    /**
     * Computes the target point at certain lookahead distance from the nearest point in the LINE
     * created between the next two nodes in the path
     *
     * @param initialPosition Initial position along the path.
     * @param targetPosition  Target position along the path.
     * @param nearestPoint    Nearest position to the initial position along the path.
     * @return Coordinates of the target point at lookahead distance
     */
    private double[] getGoalPointAtLookaheadDistance(
            double[] initialPosition, double[] targetPosition, double[] nearestPoint) {
        // TODO (playerfive): use lookahead distance proportional to robot max speed -> 0.5 *
        // getRobotMaxSpeed()
        // TODO: if the calculated curvature is 0.5, the robot should run at 50 percent of its
        // maximum velocity.
        return Geometry.getPointAtDistance(
                initialPosition, targetPosition, nearestPoint, mLookaheadDistance);
    }

    /**
     * Computes the curvature path to navigate the robot from the current position to the target
     * node. The next node is used to obtain the direction of the curvature and reach a smoother
     * path.
     *
     * @param initialTransform Robot transform
     * @param targetNode       Target node
     * @param nextNode         Next node
     * @return Curvature parameter
     */
    public double computeCurvatureToReach(
            Transform initialTransform, double[] targetNode, double[] nextNode) {
        // Takes the closest point from robot in the line generated by the goal node and the next
        double[] nearestPoint = Geometry.getIntersectionBetweenLineAndPerpendicularPoint(
                targetNode, nextNode, initialTransform.getPosition());
        // Generates a goal point from the nearest point distanced at a lookahead distance
        double[] goalPoint = getGoalPointAtLookaheadDistance(targetNode, nextNode, nearestPoint);
        // Computes the rotation necessary to reach the goal
        // TODO: If curvature * {mAngularModification} > ANGULAR_VELOCITY_LIMIT -> Saturate
        return Geometry.computeCurvature(
                initialTransform.getPosition(), initialTransform.getRotationZ(), goalPoint)
                * mAngularModification;
    }

    /**
     * Checks if the robot is near the nextNode
     *
     * @param currentPose The current robot pose
     * @param nextNode    Next node coordinates in (x, y)
     * @return True if the distance to the next node is within the look ahead distance.
     */
    public boolean isCloseToNode(Transform currentPose, double[] nextNode) {
        double distToGoal =
                Geometry.getDistanceBetweenTwoPoints(currentPose.getPosition(), nextNode);
        return distToGoal <= mLookaheadDistance;
    }

}
