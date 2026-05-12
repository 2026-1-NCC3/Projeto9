package com.example.pi_maya;

import android.app.Application;

import com.example.pi_maya.core.network.MayaApiClient;
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.data.repository.AppointmentRepositoryImpl;
import com.example.pi_maya.data.repository.AuthRepositoryImpl;
import com.example.pi_maya.data.repository.ChatRepositoryImpl;
import com.example.pi_maya.data.repository.ContentRepositoryImpl;
import com.example.pi_maya.data.repository.ExerciseRepositoryImpl;
import com.example.pi_maya.data.repository.PatientRepositoryImpl;
import com.example.pi_maya.domain.repository.AppointmentRepository;
import com.example.pi_maya.domain.repository.AuthRepository;
import com.example.pi_maya.domain.repository.ChatRepository;
import com.example.pi_maya.domain.repository.ContentRepository;
import com.example.pi_maya.domain.repository.ExerciseRepository;
import com.example.pi_maya.domain.repository.PatientRepository;

/**
 * Application + ServiceLocator manual.
 *
 * Arquitetura: o app conversa com o servidor API (Next.js na Vercel) para tudo,
 * exceto o realtime do chat — esse continua direto Supabase via WebSocket.
 */
public class MayaApp extends Application {

    private static MayaApp instance;

    private SessionManager sessionManager;
    private MayaApiClient apiClient;

    private AuthRepository authRepository;
    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private ExerciseRepository exerciseRepository;
    private ChatRepository chatRepository;
    private ContentRepository contentRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        sessionManager = new SessionManager(this);
        apiClient = new MayaApiClient(sessionManager);

        authRepository = new AuthRepositoryImpl(apiClient, sessionManager);
        patientRepository = new PatientRepositoryImpl(apiClient);
        appointmentRepository = new AppointmentRepositoryImpl(apiClient);
        exerciseRepository = new ExerciseRepositoryImpl(apiClient);
        chatRepository = new ChatRepositoryImpl(apiClient, sessionManager);
        contentRepository = new ContentRepositoryImpl(apiClient);
    }

    public static MayaApp get() {
        return instance;
    }

    public SessionManager getSessionManager() { return sessionManager; }
    public MayaApiClient getApiClient() { return apiClient; }
    public AuthRepository getAuthRepository() { return authRepository; }
    public PatientRepository getPatientRepository() { return patientRepository; }
    public AppointmentRepository getAppointmentRepository() { return appointmentRepository; }
    public ExerciseRepository getExerciseRepository() { return exerciseRepository; }
    public ChatRepository getChatRepository() { return chatRepository; }
    public ContentRepository getContentRepository() { return contentRepository; }
}
