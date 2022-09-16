// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.lang.Math;
import edu.wpi.first.wpilibj.IterativeRobotBase;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.MotorSafety;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;


/**
 * This is a demo program showing the use of the DifferentialDrive class. Runs the motors with
 * arcade steering.
 */
public class Robot extends TimedRobot {
  Timer timer= new Timer();
  float speedIncrement;
  float shotSpeed;
  private final int MOTOR_OFF=0;
  private final int camWidth=640;
  private final int camHeight=480;
  private final double TURNSPEED= 0.15;
  private final int PIXEL_OFFSET= 3;
  private final float SHOOTER_FINETUNE = (float) 0.01;
  private final float SHOOTER_COURSETUNE = (float) 0.05;
  private final Spark leftFront = new Spark(3); //variable for front left motor
  private final Spark leftBack = new Spark(4);
  private final Spark rightFront = new Spark(1);
  private final Spark rightBack = new Spark(2);
  private final Spark intake = new Spark(0);
  private final PWMSparkMax shooter = new PWMSparkMax(5);
  private final MotorControllerGroup shooting = new MotorControllerGroup(shooter,intake);
  private final MotorControllerGroup leftGroup = new MotorControllerGroup(leftFront, leftBack);
  private final MotorControllerGroup rightGroup = new MotorControllerGroup(rightFront, rightBack);
  private final DifferentialDrive robotDrive = new DifferentialDrive (leftGroup, rightGroup);
  private final Joystick m_stick = new Joystick(0);
  private final XboxController logiController = new XboxController(1); // 1 is the USB Port to be used as indicated on the Driver Station
  private NetworkTable datatable;
  String autoName="low goal";
  @Override
  public void robotInit() {
    // We need to invert one side of the drivetrain so that positive voltages
    // result in both sides moving forward. Depending on how your robot's
    // gearbox is constructed, you might have to invert the left side instead.
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    datatable = inst.getTable("datatable");
    rightBack.setInverted(true);
    rightFront.setInverted(true);
    robotDrive.setSafetyEnabled(true);
    shooter.setInverted(true);
    String[] list={"low goal", "high goal", "back up", "It's complicated", "It's complicated (no Cam)"};
    SmartDashboard.putStringArray("Auto List", list);
    
  }

  @Override
  public void autonomousInit () {
  /*back up until xyvalue of camera reaches favored point that
  we know works and then shot
  later we could add picking up an additional ball before shooting 
  */
  //add start delay
  //method one backup shoot high hoop    
  // method two shoot low hoop then back up out of
  // method three (start backwards)drive to ball pick it up turn around till we see the hoop then shoot
  timer.reset();
  timer.start();

  //int goalY=0;  
  //backupCam((double) 0.5,datatable.getEntry("vision_Y").getValue().getDouble(),goalY);
  this.autoName = SmartDashboard.getString("Auto Selector", "low goal");
  System.out.println(autoName);
  }

  @Override
  public void autonomousPeriodic(){
    switch(this.autoName){
      default:
      case "low goal":
        if(timer.get()<=3){
          shooter.set(.5);
         }
         if(timer.get()<=5 && timer.get()>3){
          shooting.set(.5);
         }
         else if(timer.get()<=7&& timer.get()>6){
          intake.stopMotor();
          shooter.stopMotor();
          this.robotDrive.arcadeDrive(-0.6, 0);
         }
       break;
      case "high goal":
      if(timer.get()<=2){
        shooter.set(0.7);
        this.robotDrive.arcadeDrive(-0.6, 0);
        //MotorSafety.checkMotors();
      }
      if(timer.get()<=4 && timer.get()>2){
       shooter.set(0.7);
       intake.set(1);
      }
      else if(timer.get()<=7&& timer.get()>6){
        intake.stopMotor();
        shooter.stopMotor();
        
      }
        break;
      case "back up":
      if(timer.get()<=1&& timer.get()>0){
        this.robotDrive.arcadeDrive(-0.6, 0);
      }
        break;


      case "It's complicated":
      Double turnDone= 15.0;
      if(timer.get()<=3&& timer.get()>0){
        this.robotDrive.arcadeDrive(0.6, 0);
        intake.set(0.5);
      }
      if(timer.get()<=3.75&& timer.get()>3.25){
        intake.set(-0.5);
      }
      if(timer.get()<=4&& timer.get()>3.75){
        intake.stopMotor();
      }
      if(timer.get()<=turnDone&& timer.get()>4){
        intake.stopMotor();
        int center = (int) Math.abs(camWidth-SmartDashboard.getNumber("DB/Slider 0", 1000000));
        while (center>=PIXEL_OFFSET) {
          robotDrive.arcadeDrive(0, TURNSPEED);
        }
        if(center<=PIXEL_OFFSET) {
          turnDone = timer.get();
        }
      }
      if(timer.get()>turnDone){
        int vertical = (int) Math.abs(camHeight-SmartDashboard.getNumber("DB/Slider 1", 1000000));
        while (PIXEL_OFFSET>vertical){
          robotDrive.arcadeDrive(0.6, 0);
        }
        shooter.set(1);
        intake.set(0.5);
      }
      break;



      case "It's complicated (no Cam)":
      if(timer.get()<=3&& timer.get()>0){
        this.robotDrive.arcadeDrive(0.6, 0);
        intake.set(0.5);
      }
      if(timer.get()<=3.75&& timer.get()>3.25){
        intake.set(-0.5);
      }
      if(timer.get()<=4&& timer.get()>3.75){
        intake.stopMotor();
      }
      if(timer.get()<=5&& timer.get()>4){
        intake.stopMotor();
        robotDrive.arcadeDrive(0, TURNSPEED);
      }

      if(timer.get()<=6&& timer.get()>5){
        robotDrive.arcadeDrive(0.6, 0);
      }
      if(timer.get()<=7&& timer.get()>6){
        shooter.set(1);
        robotDrive.arcadeDrive(0, 0);
      }
      if(timer.get()<=14&& timer.get()>7){
        shooter.set(1);
        intake.set(1);
      }
      break;
      
    }
    //this.backup(.1,2000);
    

    
    //SmartDashboard.updateValues();
  }

  @Override
  public void teleopInit () {
    shotSpeed= (float) 0.8;
  }

  @Override
  public void teleopPeriodic() {
    // Drive with arcade drive.
    // That means that the Y axis drives forward
    // and backward, and the X turns left and right.
	  
    robotDrive.arcadeDrive((-logiController.getRawAxis(5)),(logiController.getRawAxis(4)/1.5));

    intake.set((logiController.getRawButton(5)?.5:(-logiController.getRawAxis(6))));

    /*if(logiController.getAButton() || logiController.getBButton()){
      double speed = CamYToSpeed(logiController.getBButton());
      shooter.set(speed);
    }
    else{
      shooter.set((logiController.getRawButton(6)?.5:(-logiController.getRawAxis(5))));
    }
    */
    SmartDashboard.putNumber("DB/Slider 2", shotSpeed);
    if (logiController.getBButtonPressed()) {
    	shotSpeed += 0.05;
    }
    if (logiController.getAButtonPressed()) {
    	shotSpeed -= 0.05;
    }
    shooter.set(logiController.getXButton()?shotSpeed:(logiController.getRawButton(6)?.5:0.0));
    /*if (m_stick.getRawButtonPressed(2)) {
    	speedIncrement= (speedIncrement==SHOOTER_COURSETUNE)?SHOOTER_FINETUNE:SHOOTER_COURSETUNE;
    }
    if (m_stick.getRawButtonPressed(4)) {
    	shotSpeed += speedIncrement;
    }
    if (m_stick.getRawButtonPressed(3)) {
    	shotSpeed -= speedIncrement;
    } 
    */
      //shooter.set(m_stick.getRawButton(1)?shotSpeed:MOTOR_OFF);
      //intake.set(m_stick.getRawButton(12)?shotSpeed:MOTOR_OFF);
      
    //SmartDashboard.putNumber("DB/Slider 0", shotSpeed);
  }

  //function cam y to shooter speed
  public double CamYToSpeed(boolean top){
    double YPower;
    int Y = (int) SmartDashboard.getNumber("DB/Slider 1", 0.0);
    if (top) {
      YPower = .8+.2*(Math.sqrt(2*Y/camHeight));
    }
    else {
      YPower = .5+.5*(Math.sqrt(2*Y/camHeight));
    }
    return YPower;
  }


}
