package org.firstinspires.ftc.teamcode.powerplayV2;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.powerplay.SliderCommand;
import org.firstinspires.ftc.teamcode.powerplay.SliderSubsystem;
import org.firstinspires.ftc.teamcode.powerplayV2.commands.ConeCollection;
import org.firstinspires.ftc.teamcode.powerplayV2.commands.ResetSliderBasket;
import org.firstinspires.ftc.teamcode.powerplayV2.commands.SlidersGroup;
import org.firstinspires.ftc.teamcode.robotbase.TelemetrySubsystem;

public class AutonomousCommandWOPassThrough extends CommandBase {
    // Constants
    ElevatorSubsystem.Level LEVEL = ElevatorSubsystem.Level.HIGH;
    HardwareMap hardwareMap;

    //Subsystems
    ClawSubsystem claw;
    ElevatorSubsystem elevator;
    BasketSubsystem basket;
    ArmSubsystem arm;
    TelemetrySubsystem telemetryEx;

    private int index;

    SequentialCommandGroup actions;
//    ClawCommand actions;

    public AutonomousCommandWOPassThrough(int index, ClawSubsystem claw,
                                          ElevatorSubsystem elevator, BasketSubsystem basket,
                                          ArmSubsystem arm, TelemetrySubsystem telemetryEx) {
        this.hardwareMap = hardwareMap;

        this.index = index;

        this.claw = claw;
        this.elevator = elevator;
        this.basket = basket;
        this.arm = arm;
        this.telemetryEx = telemetryEx;

        telemetryEx.addMonitor("Constructor was Scheduled: ", () -> index);

        actions = new SequentialCommandGroup(
                new InstantCommand(claw::grab, claw),
                new WaitCommand(600),
                new InstantCommand(arm::setTravel),
                new WaitCommand(600),
                new InstantCommand(claw::release, claw),
                new WaitCommand(700),
                new InstantCommand(arm::setMid, arm),
                new WaitCommand(100),
                new ParallelCommandGroup(
                        new SequentialCommandGroup(
                                new ElevatorCommand(elevator, ElevatorSubsystem.Level.HIGH),
                                new InstantCommand(basket::setOuttake, basket), //Outtake the Cone
                                new WaitCommand(1500),
                                new ResetSliderBasket(elevator, basket)
                        ),
                        new InstantCommand(arm::setIntake, arm)
//                        new ArmAutoCommand(arm, () -> index)
                ),
                new WaitCommand(1000),
                ////////////////
                new InstantCommand(claw::grab, claw),
                new WaitCommand(600),
                new InstantCommand(arm::setTravel),
                new WaitCommand(600),
                new InstantCommand(claw::release, claw),
                new WaitCommand(700),
                new InstantCommand(arm::setMid, arm),
                new WaitCommand(100),
                new ParallelCommandGroup(
                        new SequentialCommandGroup(
                                new ElevatorCommand(elevator, ElevatorSubsystem.Level.HIGH),
                                new InstantCommand(basket::setOuttake, basket), //Outtake the Cone
                                new WaitCommand(1500),
                                new ResetSliderBasket(elevator, basket)
                        ),
                        new InstantCommand(arm::setIntake, arm)
//                        new ArmAutoCommand(arm, () -> index)
                ),
                new WaitCommand(1000)
        );

//        actions = new ClawCommand(claw);

        addRequirements(claw, elevator, basket, arm);
    }

    @Override
    public void initialize() {
        actions.schedule();
    }

//    @Override
//    public void execute() {
//        if (index < 5 && actions.isFinished()) {
//            actions.schedule();
//            ++index;
//        }
//    }

    @Override
    public void end(boolean interrupted) {
        telemetryEx.addMonitor("End was Scheduled: ", () -> index);

//        if(index < 5) this(hardwareMap, ++index).schedule();
    }

    @Override
    public boolean isFinished() {
        return actions.isFinished();
//        return index >= 5 && actions.isFinished();
    }
}