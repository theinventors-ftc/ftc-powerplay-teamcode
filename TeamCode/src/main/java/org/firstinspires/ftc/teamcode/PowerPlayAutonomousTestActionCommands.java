package org.firstinspires.ftc.teamcode;

import static org.inventors.ftc.robotbase.RobotEx.OpModeType.AUTO;

import com.acmerobotics.roadrunner.Pose2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.AprilTagDetectionSubsystem;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.PowerPlayRobot;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.RoadRunnerSubsystemNew;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.commands.ElevatorCommand;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.subsystems.ArmSubsystem;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.subsystems.BasketSubsystem;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.subsystems.ClawSubsystem;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.subsystems.ConeDetectorSubsystem;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.subsystems.ElevatorSubsystem;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.subsystems.FrontSliderSubsystem;
import org.firstinspires.ftc.teamcode.PowerPlayRobot.subsystems.LimitSwitchSubsystem;
import org.inventors.ftc.robotbase.drivebase.MecanumDriveSubsystem;
import org.inventors.ftc.robotbase.hardware.GamepadExEx;

import java.util.HashMap;

@Autonomous(name = "AutoOneConeTest", group = "Final Autonomous")
public class PowerPlayAutonomousTestActionCommands extends CommandOpMode {
    PowerPlayRobot robot;

    protected MecanumDriveSubsystem.Params RobotConstants;

    protected ElapsedTime runtime;
    protected MecanumDriveSubsystem drive;
    protected RoadRunnerSubsystemNew RR;
    protected AprilTagDetectionSubsystem april_tag;
    protected ClawSubsystem claw;
    protected ElevatorSubsystem elevator;
    protected BasketSubsystem basket;
    protected ArmSubsystem arm;
    protected FrontSliderSubsystem frontSlider;
    protected LimitSwitchSubsystem rightSwitch, leftSwitch;
    protected ConeDetectorSubsystem cone_detector;
    protected boolean april_tag_found = false;
    Telemetry dashboardTelemetry;
    protected SequentialCommandGroup scoringCommand;
    protected InstantCommand armCommand;
    protected Pose2d pose = new Pose2d(0, 0, 0);
    @Override
    public void initialize() {
        GamepadExEx driverOp = new GamepadExEx(gamepad1);
        GamepadExEx toolOp = new GamepadExEx(gamepad2);

        RobotConstants = new MecanumDriveSubsystem.Params();

        robot = new PowerPlayRobot(hardwareMap, RobotConstants, telemetry, driverOp, toolOp, AUTO, true,
                false);

        drive = new MecanumDriveSubsystem(hardwareMap, pose, AUTO);

        RR = new RoadRunnerSubsystemNew(drive);

        april_tag = new AprilTagDetectionSubsystem(robot.camera, dashboardTelemetry);

        claw = new ClawSubsystem(hardwareMap);
        elevator = new ElevatorSubsystem(hardwareMap);
        basket =  new BasketSubsystem(hardwareMap);
        arm = new ArmSubsystem(hardwareMap);
        rightSwitch = new LimitSwitchSubsystem(hardwareMap, "rightSwitch");
        leftSwitch = new LimitSwitchSubsystem(hardwareMap, "leftSwitch");
        frontSlider = new FrontSliderSubsystem(hardwareMap, () -> rightSwitch.getState(),
                () -> leftSwitch.getState());
        cone_detector = new ConeDetectorSubsystem(hardwareMap, 30);

        scoringCommand = new SequentialCommandGroup(
                new InstantCommand(arm::setMid, arm),
                new WaitCommand(300),
                new ElevatorCommand(elevator, ElevatorSubsystem.Level.AUTO_SCORING),
                new InstantCommand(basket::setOuttake, basket),
                new WaitCommand(1500),
                new ParallelCommandGroup(
                        new InstantCommand(basket::setTravel, basket),
                        new InstantCommand(arm::setMid, arm)
                ),
                new ElevatorCommand(elevator, ElevatorSubsystem.Level.LOW)
        );
                runtime = new ElapsedTime();
    }

    public void waitForStart() {
        /////////////////////////////////// Recognizing the Tag ///////////////////////////////////
        while (!isStarted() && !isStopRequested()) {
            april_tag_found = april_tag.aprilTagCheck();
            sleep(20);
        }
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initialize();
        waitForStart();
        runtime.reset();

        ///////////////////////////////// Running the Trajectories /////////////////////////////////
        if (isStopRequested()) return;

        schedule(new InstantCommand(RR::TEST, RR));

        //Select Command Auto
        new Trigger(() -> runtime.seconds() >= 20).whenActive(
                new SelectCommand(
                        new HashMap<Object, Command>() {{
                            put(april_tag.LEFT, new InstantCommand(RR::runPARKING_3, RR));
                            put(april_tag.MIDDLE, new InstantCommand(RR::runPARKING_2, RR));
                            put(april_tag.RIGHT, new InstantCommand(RR::runPARKING_1, RR));
                            put(-1, new InstantCommand(RR::runPARKING_2, RR));
                        }},
                        () -> april_tag_found ? april_tag.getTagOfInterest().id : -1
                )
        );

        // run the scheduler
        while (!isStopRequested() && opModeIsActive()) {
            telemetry.addData("Runtime", runtime.seconds());
            telemetry.update();
            run();
        }

        reset();
    }
}