--DROP SCHEMA public CASCADE;
--CREATE SCHEMA public;

CREATE TABLE public.usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ativo boolean NOT NULL,
    login character varying(100) NOT NULL,
    email character varying(255) NOT NULL,
    nome character varying(255) NOT NULL,
    created_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by bigint NULL,
    updated_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by bigint NULL
);
ALTER TABLE ONLY public.usuario ADD CONSTRAINT usuario_email_key UNIQUE (email);
ALTER TABLE ONLY public.usuario ADD CONSTRAINT usuario_login_key UNIQUE (login);

CREATE TABLE public.perfil (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   nome character varying(30) NOT NULL
);
ALTER TABLE ONLY public.perfil ADD CONSTRAINT perfil_nome_key UNIQUE (nome);

CREATE TABLE public.usuario_perfil (
   usuario_id bigint NOT NULL,
   perfil_id bigint NOT NULL,
   CONSTRAINT fk_usuper_usuario_id   FOREIGN KEY (usuario_id) REFERENCES usuario(id),
   CONSTRAINT fk_usuper_perfil_id   FOREIGN KEY (perfil_id) REFERENCES perfil(id)
);
ALTER TABLE ONLY public.usuario_perfil ADD CONSTRAINT usuario_perfil_pkey PRIMARY KEY (perfil_id, usuario_id);

CREATE TABLE public.parametrizacao (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   ativo boolean NOT NULL,
   perc_aleatorio numeric(5,2),
   perc_diretoria numeric(5,2),
   perc_material numeric(5,2),
   perc_temporal numeric(5,2),
   recorrencia_material integer,
   tempo_sem_auditoria integer,
   versao character varying(50) NOT NULL,
   observacao text,
   created_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   created_by bigint NOT NULL,
   updated_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_by bigint NOT NULL,
   CONSTRAINT fk_paramet_created_by   FOREIGN KEY (created_by) REFERENCES usuario(id),
   CONSTRAINT fk_paramet_updated_by   FOREIGN KEY (updated_by) REFERENCES usuario(id)
);

CREATE TABLE public.campanha (
     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     competencia integer NOT NULL,
     qtd_amostra integer,
     qtd_universo integer,
     aprovacao timestamp(6) with time zone,
     execucao_motor timestamp(6) with time zone,
     parametrizacao_id bigint NOT NULL,
     created_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
     created_by bigint NOT NULL,
     updated_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_by bigint NOT NULL,
     CONSTRAINT fk_campanha_created_by FOREIGN KEY (created_by) REFERENCES usuario(id),
     CONSTRAINT fk_campanha_updated_by FOREIGN KEY (updated_by) REFERENCES usuario(id),
     CONSTRAINT fk_campanha_paramet FOREIGN KEY (parametrizacao_id) REFERENCES parametrizacao (id)
);

CREATE TABLE public.auditoria (
      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
      pagamento date,
      valor numeric(15,2),
      campanha_id bigint NOT NULL,
      checklist_id bigint,
      conclusao timestamp(6) with time zone,
      inicio timestamp(6) with time zone,
      status character varying(50) NOT NULL,
      classificacao character varying(100),
      contrato character varying(100),
      criterio character varying(100),
      departamento character varying(100),
      diretoria character varying(100),
      processo character varying(100) NOT NULL,
      objeto character varying(1000),
      fornecedor character varying(255),
      created_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
      created_by bigint NOT NULL,
      updated_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_by bigint NOT NULL,
      CONSTRAINT fk_audit_created_by FOREIGN KEY (created_by) REFERENCES usuario(id),
      CONSTRAINT fk_audit_updated_by FOREIGN KEY (updated_by) REFERENCES usuario(id),
      CONSTRAINT fk_audit_campanha   FOREIGN KEY (campanha_id) REFERENCES campanha (id)
    );

CREATE TABLE public.auditoria_historico (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pagamento date,
    valor numeric(15,2),
    auditoria_des_id bigint,
    auditoria_ori_id bigint,
    classificacao character varying(100),
    contrato character varying(100),
    diretoria character varying(100),
    processo character varying(100),
    fornecedor character varying(255),
    motivo text NOT NULL,
    created_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by bigint NOT NULL,
    CONSTRAINT fk_hist_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE TABLE public.auditoria_legado (
     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     referencia integer,
     contrato character varying(255),
     diretoria character varying(255),
     fornecedor character varying(255),
     processo character varying(255),
     setor character varying(255),
     created_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.auditoria_auditor (
      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

      auditoria_id bigint NOT NULL,
      auditor_id bigint NOT NULL,

      inicio timestamp(6) with time zone,
      fim timestamp(6) with time zone,
      motivo_encerramento character varying(255),

      created_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
      created_by bigint NOT NULL,
      updated_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_by bigint NOT NULL,
      CONSTRAINT fk_audit_auditor_created_by FOREIGN KEY (created_by) REFERENCES usuario(id),
      CONSTRAINT fk_audit_auditor_updated_by FOREIGN KEY (updated_by) REFERENCES usuario(id),
      CONSTRAINT fk_audit_auditor_auditoria FOREIGN KEY (auditoria_id) REFERENCES auditoria (id),
      CONSTRAINT fk_audit_auditor_auditor FOREIGN KEY (updated_by) REFERENCES usuario(id)
);

CREATE INDEX idx_campanha_parametrizacao_id ON public.campanha(parametrizacao_id);
CREATE INDEX idx_auditoria_campanha_id ON public.auditoria(campanha_id);
CREATE INDEX idx_auditoria_historico_audit_des_id ON public.auditoria_historico (auditoria_des_id);
CREATE UNIQUE INDEX uq_auditoria_auditor_ativo ON auditoria_auditor (auditoria_id, auditor_id) WHERE fim IS NULL;