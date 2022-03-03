// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.cameraserver.CameraServer;

/**
 * This is a demo program showing the use of the DifferentialDrive class. Runs the motors with
 * arcade steering.
 */
public class Robot extends TimedRobot {
  float speedIncrement;
  float shotSpeed;
  private final int MOTOR_OFF=0;
  private final float SHOOTER_FINETUNE = (float) 0.01;
  private final float SHOOTER_COURSETUNE = (float) 0.05;
  private final Spark leftFront = new Spark(3); //variable for front left motor
  private final Spark leftBack = new Spark(4);
  private final Spark rightFront = new Spark(1);
  private final Spark rightBack = new Spark(2);
  private final Spark intake = new Spark(0);
  private final PWMSparkMax shooter = new PWMSparkMax(5);
  private final MotorControllerGroup leftGroup = new MotorControllerGroup(leftFront, leftBack);
  private final MotorControllerGroup rightGroup = new MotorControllerGroup(rightFront, rightBack);
  private final DifferentialDrive robotDrive = new DifferentialDrive (leftGroup, rightGroup);
  private final Joystick m_stick = new Joystick(0);

  @Override
  public void robotInit() {
    // We need to invert one side of the drivetrain so that positive voltages
    // result in both sides moving forward. Depending on how your robot's
    // gearbox is constructed, you might have to invert the left side instead.
    rightBack.setInverted(true);
    rightFront.setInverted(true);
    CameraServer.startAutomaticCapture();
  }

  @Override
  public void teleopPeriodic() {
    // Drive with arcade drive.
    // That means that the Y axis drives forward
    // and backward, and the X turns left and right.
	  
    robotDrive.arcadeDrive(-m_stick.getY(), m_stick.getZ());
    if (m_stick.getRawButtonPressed(2)) {
    	speedIncrement= (speedIncrement==SHOOTER_COURSETUNE)?SHOOTER_FINETUNE:SHOOTER_COURSETUNE;
    }
    if (m_stick.getRawButtonPressed(4)) {
    	shotSpeed += speedIncrement;
    }
    if (m_stick.getRawButtonPressed(3)) {
    	shotSpeed -= speedIncrement;
    }
      shooter.set(m_stick.getRawButton(1)?shotSpeed:MOTOR_OFF);
      intake.set(m_stick.getRawButton(12)?shotSpeed:MOTOR_OFF);
    SmartDashboard.putNumber("DB/Slider 0", shotSpeed);
  }

}
