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
import edu.wpi.first.cameraserver.CameraServer;//will be deleted

/**
 * This is a demo program showing the use of the DifferentialDrive class. Runs the motors with
 * arcade steering.
 */
public class Robot extends TimedRobot {
  float speedIncrement;
  float shotSpeed;
  private final int MOTOR_OFF=0;
  Timer timer= new Timer();
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
  String autoName;
  @Override
  public void robotInit() {
    // We need to invert one side of the drivetrain so that positive voltages
    // result in both sides moving forward. Depending on how your robot's
    // gearbox is constructed, you might have to invert the left side instead.
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    datatable = inst.getTable("datatable");
    rightBack.setInverted(true);
    rightFront.setInverted(true);
    CameraServer.startAutomaticCapture();
    robotDrive.setSafetyEnabled(true);
    shooter.setInverted(true);
    String[] list={"low goal", "high goal", "back up", "It's complicated"};
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
  autoName = SmartDashboard.getString("Auto Selector", "back up");

  }
  @Override
  public void autonomousPeriodic(){
    switch(autoName){
      case "low goal":
      if(timer.get()<=5){
        shooter.set(.5);
        //MotorSafety.checkMotors();
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
        shooter.set(1);
        this.robotDrive.arcadeDrive(-0.6, 0);
        //MotorSafety.checkMotors();
      }
      if(timer.get()<=4 && timer.get()>2){
       shooting.set(1);
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
      if(timer.get()<=3&& timer.get()>0){
        this.robotDrive.arcadeDrive(0.6, 0);
        intake.set(0.5);
      }
      if(timer.get()<=3.75&& timer.get()>3.25){
        intake.set(-0.5);
      }
      if(timer.get()<=3.75&& timer.get()>3.25){
        intake.set(-0.5);
      }
      if(timer.get()>4){
        intake.stopMotor();
        int center = (int) Math.abs(SmartDashboard.getNumber("x", 1000000000));
        while (center>=PIXEL_OFFSET) {
          robotDrive.arcadeDrive(0, TURNSPEED);
        }
        
      }

      break;

    }
    //this.backup(.1,2000);
    

    
    //SmartDashboard.updateValues();
  }

  @Override
  public void teleopPeriodic() {
    // Drive with arcade drive.
    // That means that the Y axis drives forward
    // and backward, and the X turns left and right.
	  
    robotDrive.arcadeDrive(-m_stick.getY(),m_stick.getZ());
    if (m_stick.getRawButtonPressed(2)) {
    	speedIncrement= (speedIncrement==SHOOTER_COURSETUNE)?SHOOTER_FINETUNE:SHOOTER_COURSETUNE;
    }
    if (m_stick.getRawButtonPressed(4)) {
    	shotSpeed += speedIncrement;
    }
    if (m_stick.getRawButtonPressed(3)) {
    	shotSpeed -= speedIncrement;
    }
      //shooter.set(m_stick.getRawButton(1)?shotSpeed:MOTOR_OFF);
      //intake.set(m_stick.getRawButton(12)?shotSpeed:MOTOR_OFF);
      shooter.set((logiController.getRawButton(6)?.5:(-logiController.getRawAxis(5))));
      intake.set((logiController.getRawButton(5)?.5:(-logiController.getRawAxis(1))));
    SmartDashboard.putNumber("DB/Slider 0", shotSpeed);
  }

  //function wait
  public void wait(int durationInMilli){
    long startTime = System.currentTimeMillis();
    long endTime = startTime + durationInMilli;
    while (System.currentTimeMillis() >= endTime);
    }


  //function back up
  public void backup(double speed,int durationInMilli) {
    
    long startTime = System.currentTimeMillis();
    long endTime = startTime + durationInMilli;
    while (System.currentTimeMillis() >= endTime){
      robotDrive.arcadeDrive(speed, 0);
    }
    robotDrive.arcadeDrive(0, 0);
  }

  public void backupCam(double speed,double y,int goalY) {
    robotDrive.arcadeDrive(speed, 0);
    while (goalY>y);
    robotDrive.arcadeDrive(0,0);
  }
  //funtion go forward
  public void forward(double speed,int durationInMilli) {
    robotDrive.arcadeDrive(speed, 0);
    wait(durationInMilli);
    robotDrive.arcadeDrive(0, 0);
  }
  //function Shoot (high) w/camera
  public void HighShotCam(int x,int y) {
    int direction=(x>0)?1:-1;
    // change x=0 to range
    int center = Math.abs(x);
    while (center>=PIXEL_OFFSET) {
      robotDrive.arcadeDrive(0, TURNSPEED*direction);
    }
    robotDrive.arcadeDrive(0, 0);
    double CamShootPower = ((y+100)/200);//change this code later for arc of ball and how that relates to 
    shooter.set(CamShootPower);
    intake.set(.5);
  }  
  //function Shoot (high) w/o camera
  public void HighShot(double power,int timeInMilli) {
    shooter.set(power);
    intake.set(.5);
    wait(timeInMilli);
    shooter.set(0);
    intake.set(0);
  }  
  //function shoot (low)
  public void LowShot(double power) {
    shooter.set(power);
    intake.set(.5);
  }  
  //function pickup ball/intake
  public void SuckySuckSuck(double speed,double duration) {
    intake.set(speed);
    //while duration
  }


}
